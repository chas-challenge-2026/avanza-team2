#include "fx_rates.h"

#include "ecb_source.hpp"

namespace {

/*
TODO(#57): cache the fetched table and honour `_date` for historical lookups
against eurofxref-hist.xml. For now every call pulls the latest daily rates.
*/
std::optional<double> lookup(const char* _from, const char* _to)
{
  if (_from == nullptr || _to == nullptr)
    return std::nullopt;

  auto table = ecb::fetch_latest();
  if (!table)
    return std::nullopt;

  return table->rate(_from, _to);
}

} // namespace

extern "C" double fx_rate(const char* _from, const char* _to, long _date)
{
  (void)_date;
  return lookup(_from, _to).value_or(-1.0);
}

extern "C" double fx_convert(double _amount, const char* _from, const char* _to, long _date)
{
  (void)_date;

  auto rate = lookup(_from, _to);
  if (!rate)
    return -1.0;

  return _amount * *rate;
}
