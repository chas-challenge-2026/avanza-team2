#ifndef __RISK_H__
#define __RISK_H__

#include "simd_config.h"

#include <stdint.h>
#include <math.h>

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

/** Calculates the sharpe ratio on a set of returns
 * Takes arg _rfrate for annual risk-free rate (ex. from t-bill/bonds/overnight cash benchmark)
 * and _year_freq the frequency of returns in a year, so 252 for standard market daily returns 
 * (either can be set to zero for rateless calculation) */
double risk_calc_sharpe_ratio_double(const double* _data, size_t _n,
  double _rfrate, size_t _year_freq);

/** Calculates the volatility from an array of doubles */
double risk_calc_volatility_double(const double* _data, int _n);

/** Calculates the volatility from an array of floats */
float risk_calc_volatility_float(const float* _data, int _n);

/** Calculates the volatility from an array of int32_ts */
double risk_calc_volatility_int32_t(const int32_t* _data, int _n);

/* --- SIMD versions ---
NOTE: Have found that for volatility calculation 
when using AVX-512 it is usually not worth if data_n (doubles) is < ~550
because of overhead when assigning and so on
So for daily returns over 1 year scalar is usually better, but 5 years simd wins bigly
On AVX2 data_n should be atleast ~650 or more for simd to be worth
*/
#if HAS_SIMD

/** Calculates the volatility from an array of doubles
 * SIMD version auto detected using simd_config.h
 * NOTE: These are actually slower than scalar if using less than ~500 inputs */
double risk_calc_volatility_double_simd(const double* _data, size_t _n);

/** Calculates the volatility from an array of floats
 * SIMD version auto detected using simd_config.h 
 * NOTE: These are actually slower than scalar if using less than ~500 inputs */
float risk_calc_volatility_float_simd(const float* _data, size_t _n);

#if SIMD_I32_LEN
// Using simd on integers gets increasingly complex,
// especially if return type is also floating point.
// Need conversion functions like _mm_cvtepi32_pd or _mm256_cvtepi32_pd
// which are level-specific. I say until we need it, let's leave it
// double risk_calc_volatility_int32_simd(const int32_t* _data, size_t _n);
#endif // SIMD_I32_LEN

#endif // HAS_SIMD

#endif // __RISK_H__
