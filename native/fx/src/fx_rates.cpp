#include "fx_rates.h"

#include "rate_cache.hpp"

namespace {

std::optional<double> lookup(const char* _from, const char* _to, long _date)
{
  if (_from == nullptr || _to == nullptr)
    return std::nullopt;

  if (_date == 0) {
    auto table = fx_cache::latest();
    if (!table)
      return std::nullopt;

    return table->rate(_from, _to);
  }

  auto history = fx_cache::history();
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
