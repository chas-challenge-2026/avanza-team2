#include "rates.h"
#include "rates.hpp"

#include "ecb.hpp"
#include "history.hpp"
#include "table.hpp"

#include <chrono>
#include <ctime>
#include <memory>
#include <mutex>

namespace {

using rates::Clock;

template <typename T>
struct Cached
{
  std::shared_ptr<const T> value;
  Clock::time_point        fetched_at;
};

std::mutex        cache_mutex;
Cached<FxTable>   latest_cache;
Cached<FxHistory> history_cache;

// The fetch runs without the lock so other lookups don't wait on ECB.
// If it fails the old value is kept until the next publication, and with
// nothing cached yet every call tries again.
template <typename T, typename Fetch>
std::shared_ptr<const T> refresh(Cached<T>& _cache, Fetch _fetch)
{
  {
    std::lock_guard<std::mutex> lock(cache_mutex);
    if (_cache.value && rates::same_publication_day(_cache.fetched_at, Clock::now()))
      return _cache.value;
  }

  auto fetched = _fetch();

  std::lock_guard<std::mutex> lock(cache_mutex);
  if (fetched)
    _cache.value = std::make_shared<const T>(std::move(*fetched));
  _cache.fetched_at = Clock::now();
  return _cache.value;
}

std::optional<double> lookup(const char* _from, const char* _to, long _date)
{
  if (_from == nullptr || _to == nullptr)
    return std::nullopt;

  if (_date == 0) {
    auto table = refresh(latest_cache, ecb::fetch_latest);
    return table ? table->rate(_from, _to) : std::nullopt;
  }

  auto hist = refresh(history_cache, ecb::fetch_history);
  if (!hist)
    return std::nullopt;

  const FxTable* table = hist->at_or_before(_date);
  return table ? table->rate(_from, _to) : std::nullopt;
}

} // namespace

namespace rates {

bool same_utc_day(Clock::time_point _a, Clock::time_point _b)
{
  std::time_t a = Clock::to_time_t(_a);
  std::time_t b = Clock::to_time_t(_b);

  std::tm ta{};
  std::tm tb{};
  gmtime_r(&a, &ta);
  gmtime_r(&b, &tb);

  return ta.tm_year == tb.tm_year && ta.tm_yday == tb.tm_yday;
}

bool same_publication_day(Clock::time_point _a, Clock::time_point _b)
{
  return same_utc_day(_a - publish_offset, _b - publish_offset);
}

} // namespace rates

extern "C" double fx_rate(const char* _from, const char* _to, long _date)
{
  return lookup(_from, _to, _date).value_or(-1.0);
}

extern "C" double fx_convert(double _amount, const char* _from, const char* _to, long _date)
{
  auto rate = lookup(_from, _to, _date);
  return rate ? _amount * *rate : -1.0;
}
