#include "rates.h"

#include "ecb.hpp"
#include "history.hpp"
#include "table.hpp"

#include <chrono>
#include <ctime>
#include <memory>
#include <mutex>

namespace {

using Clock = std::chrono::system_clock;

// ECB publishes the daily rate once around 16:00 CET; the full history file
// only grows by that same one entry a day. Neither needs to be refetched
// more than once per calendar day.

std::mutex cache_mutex;

std::shared_ptr<const FxTable> latest_cached;
Clock::time_point              latest_fetched_at;

std::shared_ptr<const FxHistory> history_cached;
Clock::time_point                history_fetched_at;

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

// ECB publishes around 16:00 CET, which is 15:00 UTC in winter and 14:00 UTC
// in summer. Shifting both times back by 15:30 makes the cache roll over
// shortly after each publication instead of at midnight UTC.
constexpr auto publish_offset = std::chrono::hours(15) + std::chrono::minutes(30);

bool same_publication_day(Clock::time_point _a, Clock::time_point _b)
{
  return same_utc_day(_a - publish_offset, _b - publish_offset);
}

std::shared_ptr<const FxTable> latest()
{
  std::shared_ptr<const FxTable> cached;
  bool stale;
  {
    std::lock_guard<std::mutex> lock(cache_mutex);
    cached = latest_cached;
    stale  = !cached || !same_publication_day(latest_fetched_at, Clock::now());
  }

  if (!stale)
    return cached;

  // The blocking ECB fetch runs without the lock held, so a stale cache
  // doesn't make every concurrent caller queue behind this one HTTP call.
  auto fetched = ecb::fetch_latest();

  std::lock_guard<std::mutex> lock(cache_mutex);

  // A failed refetch keeps serving the last known good table instead of
  // dropping it; the timestamp still advances so we don't hammer ECB on
  // every call while it's unreachable.
  if (fetched)
    latest_cached = std::make_shared<const FxTable>(std::move(*fetched));
  latest_fetched_at = Clock::now();
  return latest_cached;
}

std::shared_ptr<const FxHistory> history()
{
  std::shared_ptr<const FxHistory> cached;
  bool stale;
  {
    std::lock_guard<std::mutex> lock(cache_mutex);
    cached = history_cached;
    stale  = !cached || !same_publication_day(history_fetched_at, Clock::now());
  }

  if (!stale)
    return cached;

  auto fetched = ecb::fetch_history();

  std::lock_guard<std::mutex> lock(cache_mutex);

  if (fetched)
    history_cached = std::make_shared<const FxHistory>(std::move(*fetched));
  history_fetched_at = Clock::now();
  return history_cached;
}

std::optional<double> lookup(const char* _from, const char* _to, long _date)
{
  if (_from == nullptr || _to == nullptr)
    return std::nullopt;

  if (_date == 0) {
    auto table = latest();
    return table ? table->rate(_from, _to) : std::nullopt;
  }

  auto hist = history();
  if (!hist)
    return std::nullopt;

  const FxTable* table = hist->at_or_before(_date);
  return table ? table->rate(_from, _to) : std::nullopt;
}

} // namespace

extern "C" double fx_rate(const char* _from, const char* _to, long _date)
{
  return lookup(_from, _to, _date).value_or(-1.0);
}

extern "C" double fx_convert(double _amount, const char* _from, const char* _to, long _date)
{
  auto rate = lookup(_from, _to, _date);
  return rate ? _amount * *rate : -1.0;
}
