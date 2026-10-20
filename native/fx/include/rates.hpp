#pragma once

#include <chrono>
#include <condition_variable>
#include <cstdio>
#include <exception>
#include <memory>
#include <mutex>
#include <optional>
#include <utility>

/*
The cache behind fx_rate, and the rules for when it is out of date. The
current time is passed in so the self-test can drive it.
*/

namespace rates {

using Clock = std::chrono::system_clock;

/**
 * @brief Start of the cache day. ECB publishes around 16:00 CET, which is
 * 15:00 UTC in winter and 14:00 UTC in summer.
 */
inline constexpr auto publish_offset = std::chrono::hours(15) + std::chrono::minutes(30);

/**
 * @brief How long to wait after a failed fetch before trying ECB again.
 */
inline constexpr auto retry_after = std::chrono::minutes(5);

/**
 * @brief Checks whether two times fall on the same UTC calendar day.
 * @param _a First time.
 * @param _b Second time.
 * @return True if both are on the same UTC day.
 */
bool same_utc_day(Clock::time_point _a, Clock::time_point _b);

/**
 * @brief Checks whether two times fall between the same two ECB publications.
 * @param _a Time the cached table was fetched.
 * @param _b Time of the current lookup.
 * @return True if the table fetched at _a is still current at _b.
 */
bool same_publication_day(Clock::time_point _a, Clock::time_point _b);

/**
 * @brief One cached ECB table and when it was fetched.
 */
template <typename T>
struct Cached
{
  std::mutex               mutex;
  std::condition_variable  done; // notified when a fetch finishes
  std::shared_ptr<const T> value;
  Clock::time_point        fetched_at;       // last successful fetch
  Clock::time_point        failed_at;        // last failed fetch
  bool                     fetching = false; // a lookup is fetching right now
};

/**
 * @brief Returns the cached value, fetching a new one if a publication has
 * happened since the last successful fetch. Only one lookup fetches at a
 * time, the others get the old value or wait if there is none yet.
 * @param _cache Cache to read and update.
 * @param _fetch Called without the lock held, returns std::optional<T>.
 * @param _now Current time.
 * @return The newest value available, possibly an old one if the fetch
 * failed, or nullptr if nothing has been fetched yet.
 */
template <typename T, typename Fetch>
std::shared_ptr<const T> refresh(Cached<T>& _cache, Fetch _fetch, Clock::time_point _now)
{
  {
    std::unique_lock<std::mutex> lock(_cache.mutex);
    if (_cache.fetching) {
      if (!_cache.value)
        _cache.done.wait(lock, [&] { return !_cache.fetching; });
      return _cache.value;
    }

    bool fresh   = _cache.value && same_publication_day(_cache.fetched_at, _now);
    bool backoff = _now - _cache.failed_at < retry_after;
    if (fresh || backoff)
      return _cache.value;

    _cache.fetching = true;
  }

  // No lock during the fetch, so other lookups don't wait on ECB. An
  // exception counts as a failed fetch so that fetching is always reset.
  std::optional<T> fetched;
  try {
    fetched = _fetch();
  } catch (const std::exception& _e) {
    std::fprintf(stderr, "fx: fetch threw: %s\n", _e.what());
  } catch (...) {
    std::fprintf(stderr, "fx: fetch threw an unknown exception\n");
  }

  std::shared_ptr<const T> result;
  {
    std::lock_guard<std::mutex> lock(_cache.mutex);
    if (fetched) {
      _cache.value      = std::make_shared<const T>(std::move(*fetched));
      _cache.fetched_at = _now;
    } else {
      _cache.failed_at = _now;
    }
    _cache.fetching = false;
    result          = _cache.value;
  }
  _cache.done.notify_all();
  return result;
}

} // namespace rates
