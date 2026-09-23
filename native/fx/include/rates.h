#ifndef __RATES_H__
#define __RATES_H__

/*
FX rate lookup. These are the C linkage entry points for the JNA bridge; the
implementation behind them is C++ (see table.hpp / ecb.hpp).

Rates come from the ECB euro reference rates, which are quoted against EUR, so
any pair is resolved as a cross rate through EUR:

  rate(from -> to) = (units of `to` per EUR) / (units of `from` per EUR)

`_date` is a Unix timestamp. Pass 0 for the latest daily rate; any other
value resolves to the ECB trading day at or before that date, since ECB
doesn't publish on weekends or bank holidays.
*/

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Cross rate _from -> _to.
 * @param _from Source currency code, e.g. "USD".
 * @param _to Target currency code, e.g. "SEK".
 * @param _date Unix timestamp, 0 for the latest daily rate.
 * @return The rate, or a negative value on failure.
 */
double fx_rate(const char* _from, const char* _to, long _date);

/**
 * @brief Converts _amount from _from to _to.
 * @param _amount Amount in _from.
 * @param _from Source currency code.
 * @param _to Target currency code.
 * @param _date Unix timestamp, 0 for the latest daily rate.
 * @return The converted amount, or a negative value on failure.
 */
double fx_convert(double _amount, const char* _from, const char* _to, long _date);

#ifdef __cplusplus
} // extern "C"
#endif

#endif // __RATES_H__
