#ifndef __RISK_H__
#define __RISK_H__

#include "simd_config.h"

// #include <stdlib.h>
#include <stdint.h>
#include <math.h>


/** Calculates the volatility from an array of doubles */
double risk_calc_volatility_double(double* _returns, int _n);

#if HAS_SIMD
/** Calculates the volatility from an array of doubles
 * SIMD version using avx256 (requires AVX or AVX2 to be defined) */
double risk_calc_volatility_double_avx256(const double* _returns, const size_t _n);

/** Calculates the volatility from an array of doubles
 * SIMD version auto detected using simd_config.h */
double risk_calc_volatility_double_simd(const double* _returns, size_t _n);
#endif // HAS_SIMD

#endif // __RISK_H__
