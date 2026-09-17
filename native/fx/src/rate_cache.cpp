#include "rate_cache.hpp"

#include "ecb_source.hpp"

#include <chrono>
#include <mutex>

namespace fx_cache {

namespace {

using Clock = std::chrono::steady_clock;

// ECB publishes the daily rate once around 16:00 CET; the full history file
// only grows by that same one entry a day. Neither needs to be refetched
// more often than this.

constexpr std::chrono::minutes latest_ttl{15};
constexpr std::chrono::hours   history_ttl{24};

std::mutex cache_mutex;

std::shared_ptr<const FxTable> latest_cached;
Clock::time_point              latest_fetched_at;

std::shared_ptr<const FxHistory> history_cached;
Clock::time_point                history_fetched_at;

bool expired(Clock::time_point _fetched_at, Clock::duration _ttl)
{
  return Clock::now() - _fetched_at >= _ttl;
}

} // namespace

std::shared_ptr<const FxTable> latest()
{
  std::lock_guard<std::mutex> lock(cache_mutex);

  if (!latest_cached || expired(latest_fetched_at, latest_ttl)) {
    auto fetched      = ecb::fetch_latest();
    latest_cached     = fetched ? std::make_shared<const FxTable>(std::move(*fetched)) : nullptr;
    latest_fetched_at = Clock::now();
  }

  return latest_cached;
}

std::shared_ptr<const FxHistory> history()
{
  std::lock_guard<std::mutex> lock(cache_mutex);

  if (!history_cached || expired(history_fetched_at, history_ttl)) {
    auto fetched       = ecb::fetch_history();
    history_cached     = fetched ? std::make_shared<const FxHistory>(std::move(*fetched)) : nullptr;
    history_fetched_at = Clock::now();
  }

  return history_cached;
}

} // namespace fx_cache
