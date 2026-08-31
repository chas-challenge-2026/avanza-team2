#ifndef __TEST_DATA_H__
#define __TEST_DATA_H__

#include <stdint.h>
#include <stdlib.h>


/** Generates a sample array with _n amount of randomized doubles
 * *_arr must be allocated beforehand */
void generate_sample_arr_double(double *_arr, 
  const size_t _n, 
  const double _base_start,        // Base value
  const double _base_step,         // Deviation from base value each increment
  const double _noise_magnitude);  // Fluctuation 

/** Generates a sample array with _n amount of randomized floats 
 * *_arr must be allocated beforehand */
void generate_sample_arr_float(float *_arr, 
  const size_t _n, 
  const float _base_start,         // Base value
  const float _base_step,          // Deviation from base value each increment
  const float _noise_magnitude);   // Fluctuation 

/** Generates a sample array with _n amount of randomized int32_ts 
 * *_arr must be allocated beforehand */
void generate_sample_arr_int32(int32_t *_arr, 
  const size_t _n, 
  const int32_t _base_start,       // Base value                               
  const int32_t _base_step,        // Deviation from base value each increment
  const int32_t _noise_magnitude); // Fluctuation 

/** Generates a sample array with _n amount of randomized int8_ts 
 * *_arr must be allocated beforehand */
void generate_sample_arr_int8(int8_t *_arr, 
  const size_t _n, 
  const int8_t _base_start,        // Base value
  const int8_t _base_step,         // Deviation from base value each increment
  const int8_t _noise_magnitude);  // Fluctuation 

#endif // __RISK_TESTS_H__
