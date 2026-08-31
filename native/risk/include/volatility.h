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

/** Calculates the volatility from an array of doubles */
double risk_calc_volatility_double(const double* _data, int _n);

/** Calculates the volatility from an array of floats */
float risk_calc_volatility_float(const float* _data, int _n);

/** Calculates the volatility from an array of int32_ts */
double risk_calc_volatility_int32_t(const int32_t* _data, int _n);

// --- SIMD versions ---
#if HAS_SIMD

/** Calculates the volatility from an array of doubles
 * SIMD version auto detected using simd_config.h */
double risk_calc_volatility_double_simd(const double* _data, size_t _n);

/** Calculates the volatility from an array of floats
 * SIMD version auto detected using simd_config.h */
float risk_calc_volatility_float_simd(const float* _data, size_t _n);

#if SIMD_I32_LEN
/** Calculates the volatility from an array of doubles
 * SIMD version auto detected using simd_config.h */
// Using simd on integers gets increasingly complex,
// especially if return type is also floating point.
// Need conversion functions like _mm_cvtepi32_pd or _mm256_cvtepi32_pd
// which are level-specific. I say until we need it, let's leave it
// double risk_calc_volatility_int32_simd(const int32_t* _data, size_t _n);
#endif // SIMD_I32_LEN

#endif // HAS_SIMD

#endif // __RISK_H__
