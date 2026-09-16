#include "fx_rates.h"

#include "ecb_source.hpp"

namespace {

/*
TODO(#57): cache the fetched tables and reuse them instead of refetching per
call.
*/
std::optional<double> lookup(const char* _from, const char* _to, long _date)
{
  if (_from == nullptr || _to == nullptr)
    return std::nullopt;

  if (_date == 0) {
    auto table = ecb::fetch_latest();
    if (!table)
      return std::nullopt;

    return table->rate(_from, _to);
  }

  auto history = ecb::fetch_history();
  if (!history)
    return std::nullopt;

  const FxTable* table = history->at_or_before(_date);
  if (table == nullptr)
    return std::nullopt;

  return table->rate(_from, _to);
}

} // namespace

extern "C" double fx_rate(const char* _from, const char* _to, long _date)
{
  return lookup(_from, _to, _date).value_or(-1.0);
}

extern "C" double fx_convert(double _amount, const char* _from, const char* _to, long _date)
{
  auto rate = lookup(_from, _to, _date);
  if (!rate)
    return -1.0;

  return _amount * *rate;
}
