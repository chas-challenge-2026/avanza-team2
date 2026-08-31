#ifndef __MA_H__
#define __MA_H__

#include <stdlib.h>
#include <stdint.h>


// ============================================================================
// ============================== MOVING AVERAGES =============================
// ============================================================================

/** Calculates Simple Moving Average (SMA) on set of data
 * Returns a heap allocated array with _n - _window amount of doubles
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
double* risk_calc_sma_double(const double* _data, size_t _n, size_t _window);

/** Calculates Weighted Moving Average (WMA) on set of data
 * Same as SMA but prioratizes most recent data using a weight
 * most recent data has the highest weight=_window and oldest has weight=1
 * Returns a heap allocated array with _n - _window amount of doubles
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
double* risk_calc_wma_double(const double* _data, size_t _n, size_t _window);

/** Calculates Exponentially Weighted Moving Average (EMA/EWMA) on set of data
 * Same as WMA but the weights are exponential, _window only used for alpha
 * Returns a heap allocated array with _n amount of doubles
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
double* risk_calc_ema_double(const double* _data, size_t _n, size_t _window);

/** Calculates Simple Moving Average (SMA) on set of data
 * Returns a heap allocated array with _n - _window amount of floats
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
float* risk_calc_sma_float(const float* _data, size_t _n, size_t _window);

/** Calculates Weighted Moving Average (WMA) on set of data
 * Same as SMA but prioratizes most recent data using a weight
 * most recent data has the highest weight=_window and oldest has weight=1
 * Returns a heap allocated array with _n - _window amount of floats
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
float* risk_calc_wma_float(const float* _data, size_t _n, size_t _window);

/** Calculates Exponentially Weighted Moving Average (EMA/EWMA) on set of data
 * Same as WMA but the weights are exponential, _window only used for alpha
 * Returns a heap allocated array with _n amount of floats
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
float* risk_calc_ema_float(const float* _data, size_t _n, size_t _window);

/** Calculates Simple Moving Average (SMA) on set of data
 * Returns a heap allocated array with _n - _window amount of int32_ts
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
int32_t* risk_calc_sma_int32_t(const int32_t* _data, size_t _n, size_t _window);

/** Calculates Weighted Moving Average (WMA) on set of data
 * Same as SMA but prioratizes most recent data using a weight
 * most recent data has the highest weight=_window and oldest has weight=1
 * Returns a heap allocated array with _n - _window amount of int32_ts
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
int32_t* risk_calc_wma_int32_t(const int32_t* _data, size_t _n, size_t _window);

/** Calculates Exponentially Weighted Moving Average (EMA/EWMA) on set of data
 * Same as WMA but the weights are exponential, _window only used for alpha
 * Returns a heap allocated array with _n amount of int32_ts
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
int32_t* risk_calc_ema_int32_t(const int32_t* _data, size_t _n, size_t _window);

#endif // __MA_H__ 
