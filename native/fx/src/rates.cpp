#include "rates.h"
#include "rates.hpp"

#include "ecb.hpp"
#include "history.hpp"
#include "table.hpp"

#include <chrono>
#include <ctime>
#include <memory>
#include <optional>

namespace {

using rates::Clock;

rates::Cached<FxTable>   latest_cache;
rates::Cached<FxHistory> history_cache;

// The table used for a lookup at _date, 0 meaning the latest one.
std::shared_ptr<const FxTable> table_for(long _date)
{
  if (_date == 0)
    return rates::refresh(latest_cache, ecb::fetch_latest, Clock::now());

  auto hist = rates::refresh(history_cache, ecb::fetch_history, Clock::now());
  const FxTable* table = hist ? hist->at_or_before(_date) : nullptr;
  if (!table)
    return nullptr;

  // Points at one day's table but keeps the whole history alive.
  return std::shared_ptr<const FxTable>(hist, table);
}

std::optional<double> lookup(const char* _from, const char* _to, long _date)
{
  if (_from == nullptr || _to == nullptr)
    return std::nullopt;

  auto table = table_for(_date);
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

extern "C" long fx_rate_date(long _date)
{
  auto table = table_for(_date);
  if (!table)
    return -1;

  std::tm     tm{};
  const char* end = strptime(table->date().c_str(), "%Y-%m-%d", &tm);
  if (end == nullptr || *end != '\0')
    return -1;

  return static_cast<long>(timegm(&tm));
}
