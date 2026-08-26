#ifndef __RISK_H__
#define __RISK_H__

#include "simd_config.h"

#include <stdlib.h>
#include <time.h>
#include <math.h>

/** Generates a sample array with _n amount of randomized doubles
 * *_arr must be allocated beforehand */
void generate_sample_arr(double *_arr, 
  const size_t _n, 
  const double _base_start,       // Base value
  const double _base_step,        // Deviation from base value each increment
  const double _noise_magnitude); // Fluctuation 

/** Calculates the volatility from an array of doubles */
double risk_calc_volatility_dbl_simple(double* _returns, int _n);

#if HAS_SIMD
/** Calculates the volatility from an array of doubles
 * SIMD version using avx256 (requires AVX or AVX2 to be defined) */
double risk_calc_volatility_dbl_avx256(const double* _returns, const size_t _n);

/** Calculates the volatility from an array of doubles
 * SIMD version auto detected using simd_config.h */
double risk_calc_volatility_dbl_simd(const double* _returns, size_t _n);
#endif // HAS_SIMD

#endif // __RISK_H__
