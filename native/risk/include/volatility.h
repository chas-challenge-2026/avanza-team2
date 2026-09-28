#ifndef __VOLATILITY_H__
#define __VOLATILITY_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "simd_config.h"

#include <stdint.h>
#include <math.h>

// ============================================================================
// ============================ Volatility helpers ============================
// ============================================================================

/*
Functions that specify a _returns input expect decimal series returns, 
i.e the decimal change from one value to the next, Example: [0.01, -0,03, ..]
If you have an array of cumulative asset values, Example: [100, 103, ..]
suggest converting them using data_convert_values_to_returns() from data_utils
*/

/*
Volatility is the standard deviation on a set of data.

Example:
Generating an array of 365 results
First result: 101.257940
Last result (index 364): 96.136737
Volatility: 5.873424

Here the standard/most common deviation between results is 5.873424
Meaning 68% of the time the next value will change +/- within that range
And 95% of the time within 2X that range (11.746848)

It is calculated by:
1. Mean Return: Average of all returns.
2. Squared Differences: For each return, subtract the mean and square it.
3. Sum & Divide: Sum these squares, divide by N-1 (Bessel’s correction for samples).
4. Square Root: Take the square root of divided sum to get volatility.
*/

/** Calculates the volatility from an array of series return doubles */
double risk_calc_volatility_double(const double* _returns, int _n);

/** Calculates the volatility from an array of series return floats */
float risk_calc_volatility_float(const float* _returns, int _n);

/** Calculates the volatility from an array of series return int32_ts */
double risk_calc_volatility_int32_t(const int32_t* _returns, int _n);

/* --- SIMD versions ---
NOTE: Have found that for volatility calculation 
when using AVX-512 it is usually not worth if data_n (doubles) is < ~550
because of overhead when assigning and so on
So for daily returns over 1 year scalar is usually better, but 5 years simd wins bigly
On AVX2 data_n should be atleast ~650 or more for simd to be worth
*/
#if HAS_SIMD

/** Calculates the volatility from an array of series return doubles
 * SIMD version auto detected using simd_config.h
 * NOTE: These are actually slower than scalar if using less than ~500 inputs */
double risk_calc_volatility_double_simd(const double* _returns, size_t _n);

/** Calculates the volatility from an array of series return floats
 * SIMD version auto detected using simd_config.h 
 * NOTE: These are actually slower than scalar if using less than ~500 inputs */
float risk_calc_volatility_float_simd(const float* _returns, size_t _n);

#if SIMD_I32_LEN
// Using simd on integers gets increasingly complex,
// especially if return type is also floating point.
// Need conversion functions like _mm_cvtepi32_pd or _mm256_cvtepi32_pd
// which are level-specific. I say until we need it, let's leave it
// double risk_calc_volatility_int32_simd(const int32_t* _returns, size_t _n);
#endif // SIMD_I32_LEN

#endif // HAS_SIMD

// ============================================================================
// =========================== Sharpe ratio helpers ===========================
// ============================================================================

/** Calculates the sharpe ratio on a set of series returns
 * Takes arg _rfrate for annual risk-free rate (ex. from t-bill/bonds/overnight cash benchmark)
 * and _year_freq the frequency of returns in a year, so 252 for standard market daily returns 
 * (either can be set to zero for rateless calculation) */
double risk_calc_sharpe_ratio_double(const double* _returns, size_t _n,
  double _rfrate, size_t _year_freq);

// ============================================================================
// =========================== Max drawdown helpers ===========================
// ============================================================================

/* Calculates max drawdown on cumulative asset values
 * Returns a positive fraction, so 0.25 means a 25% drop 
 * Returns 1.0 if all was lost or 0.0 if invalid input or no value change */
double risk_calc_max_drawdown(const double* _values, size_t _n);


#ifdef __cplusplus
} // extern "C"
#endif

#endif // __VOLATILITY_H__
