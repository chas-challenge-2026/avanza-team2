#ifndef __FX_RATES_H__
#define __FX_RATES_H__

/*
FX rate lookup. These are the C linkage entry points for the JNA bridge; the
implementation behind them is C++ (see fx_table.hpp / ecb_source.hpp).

Rates come from the ECB euro reference rates, which are quoted against EUR, so
any pair is resolved as a cross rate through EUR:

  rate(from -> to) = (units of `to` per EUR) / (units of `from` per EUR)

`_date` is a Unix timestamp reserved for historical lookups and is currently
ignored (see #57). Both functions return a negative value on failure.
*/

#ifdef __cplusplus
extern "C" {
#endif

double fx_rate(const char* _from, const char* _to, long _date);
double fx_convert(double _amount, const char* _from, const char* _to, long _date);

#ifdef __cplusplus
} // extern "C"
#endif

#endif // __FX_RATES_H__
