#include "volatility.h"

#include <stdio.h>

// TODO: proper logging

double risk_calc_volatility_double(const double* _data, int _n) 
{
  if (!_data || _n <= 1) return 0.0;

  // Step 1: Calculate mean
  double sum = 0.0;
  for (int i = 0; i < _n; i++) {
    sum += _data[i];
  }
  double mean = sum / _n;

  // Step 2: Calculate sum of squared differences from the mean
  double sum_sq_diff = 0.0;
  for (int i = 0; i < _n; i++) {
    double diff = _data[i] - mean;
    sum_sq_diff += diff * diff;
  }

  // Step 3: Calculate sample standard deviation
  return sqrt(sum_sq_diff / (_n - 1));
}

float risk_calc_volatility_float(const float* _data, int _n) 
{
  if (!_data || _n <= 1) return 0.0;

  // Step 1: Calculate mean
  float sum = 0.0;
  for (int i = 0; i < _n; i++) {
    sum += _data[i];
  }
  float mean = sum / _n;

  // Step 2: Calculate sum of squared differences from the mean
  float sum_sq_diff = 0.0;
  for (int i = 0; i < _n; i++) {
    float diff = _data[i] - mean;
    sum_sq_diff += diff * diff;
  }

  // Step 3: Calculate sample standard deviation
  return (float)sqrt((double)sum_sq_diff / (_n - 1));
}

double risk_calc_volatility_int32(const int32_t* _data, int _n) 
{
  if (!_data || _n <= 1) return 0.0;

  // Step 1: Calculate mean
  double sum = 0.0;
  for (int i = 0; i < _n; i++) {
    sum += (double)_data[i];
  }
  double mean = sum / _n;

  // Step 2: Calculate sum of squared differences from the mean
  double sum_sq_diff = 0.0;
  double diff;
  for (int i = 0; i < _n; i++) {
    diff = (double)_data[i] - mean;
    sum_sq_diff += diff * diff;
  }

  // Step 3: Calculate sample standard deviation
  return sqrt((double)sum_sq_diff / (_n - 1));
}

#if HAS_SIMD
double risk_calc_volatility_double_simd(const double* _data, size_t _n)
{
  // Won't matter if dereffed values aren't inited etc. but guards a lil
  if (!_data || _n < 2) return 0.0;

  printf("Calculating volatility using SIMD level: %s\n", SIMD_LEVEL);

  size_t i;
  double sum_sq_diff;
  size_t vec_i = SIMD_D_LEN; // how many doubles in each vec

  // 1. Average of all returns.
  double avg = 0.0;
  for (i = 0; i < _n; i++) avg += _data[i];
  avg /= _n;

  // 2. Squared Differences: For each return, subtract the mean and square it.
  SIMD_DOUBLE_T sum_sq_diff_v = SIMD_D_ZERO(); // create zeroed vector of 4 doubles 

  for (i = 0; i <= _n-vec_i; i += vec_i)
  {
    // Create vector with next 4 returns
    SIMD_DOUBLE_T ret_v = SIMD_D_LOADU(&_data[i]);

    // Create vector with each double set to avg
    SIMD_DOUBLE_T avg_v = SIMD_D_SET1(avg);

    // Create vector with avg subtracted from returns vectors
    SIMD_DOUBLE_T diff_v = SIMD_D_SUB(ret_v, avg_v);

    // sum_sq_diff += diff * diff; (but for vector)
    sum_sq_diff_v = SIMD_D_ADD(sum_sq_diff_v, SIMD_D_MUL(diff_v, diff_v));
  }
  // Add the partially summed vector doubles together 
  if (vec_i == 2)
  {
    double temp_v[2];
    SIMD_D_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1];
  }
  else if (vec_i == 4) {
    double temp_v[4];
    SIMD_D_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3];
  }
  else if (vec_i == 8) {
    double temp_v[8];
    SIMD_D_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3] + 
                  temp_v[4] + temp_v[5] + temp_v[6] + temp_v[7];
  }
  else {
    //TODO: error log
    return 0.0;
  }
  

  // We also need to handle remainders 
  // since if _data isn't a multiple of vec_i, 
  // last few values will be excluded from loop 
  // Finish loop regularly
  for(; i < _n; i++) {
    double diff = _data[i] - avg;
    sum_sq_diff += diff * diff;
  }

  // 3. Sum & Divide: Sum these squares, divide by N-1 (Bessel’s correction).
  // 4. Square Root: Take the square root of divided sum to get volatility.
  return sqrt(sum_sq_diff /= (_n-1));

}

float risk_calc_volatility_float_simd(const float* _data, size_t _n)
{
  // Won't matter if dereffed values aren't inited etc. but guards a lil
  if (!_data || _n < 2) return 0.0;

  printf("Calculating volatility using SIMD level: %s\n", SIMD_LEVEL);

  size_t i;
  float sum_sq_diff;
  size_t vec_i = SIMD_F_LEN; // how many doubles in each vec

  // 1. Average of all returns.
  float avg = 0.0;
  for (i = 0; i < _n; i++) avg += _data[i];
  avg /= _n;

  // 2. Squared Differences: For each return, subtract the mean and square it.
  SIMD_FLOAT_T sum_sq_diff_v = SIMD_F_ZERO(); // create zeroed vector of 4 doubles 

  for (i = 0; i <= _n-vec_i; i += vec_i)
  {
    // Create vector with next 4 returns
    SIMD_FLOAT_T ret_v = SIMD_F_LOADU(&_data[i]);

    // Create vector with each double set to avg
    SIMD_FLOAT_T avg_v = SIMD_F_SET1(avg);

    // Create vector with avg subtracted from returns vectors
    SIMD_FLOAT_T diff_v = SIMD_F_SUB(ret_v, avg_v);

    // sum_sq_diff += diff * diff; (but for vector)
    sum_sq_diff_v = SIMD_F_ADD(sum_sq_diff_v, SIMD_F_MUL(diff_v, diff_v));
  }
  // Add the partially summed vector doubles together 
  if (vec_i == 4)
  {
    float temp_v[4];
    SIMD_F_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3];
  }
  else if (vec_i == 8) {
    float temp_v[8];
    SIMD_F_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3] + 
                  temp_v[4] + temp_v[5] + temp_v[6] + temp_v[7];
  }
  else if (vec_i == 16) {
    float temp_v[16];
    SIMD_F_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3] + 
                  temp_v[4] + temp_v[5] + temp_v[6] + temp_v[7] +
                  temp_v[8] + temp_v[9] + temp_v[10] + temp_v[11] + 
                  temp_v[12] + temp_v[13] + temp_v[14] + temp_v[15];
  }
  else {
    //TODO: error log
    return 0.0;
  }
  

  // We also need to handle remainders 
  // since if _data isn't a multiple of vec_i, 
  // last few values will be excluded from loop 
  // Finish loop regularly
  for(; i < _n; i++) {
    double diff = _data[i] - avg;
    sum_sq_diff += diff * diff;
  }

  // 3. Sum & Divide: Sum these squares, divide by N-1 (Bessel’s correction).
  // 4. Square Root: Take the square root of divided sum to get volatility.
  return sqrt(sum_sq_diff /= (_n-1));

}

#if ONE_PLUS_ONE_EQUALS_FIVE
double risk_calc_volatility_int32_simd(const int32_t* _data, size_t _n)
{
  // Won't matter if dereffed values aren't inited etc. but guards a lil
  if (!_data || _n < 2) return 0.0;

  printf("Calculating volatility using SIMD level: %s\n", SIMD_LEVEL);

  size_t i;
  int32_t sum_sq_diff;
  size_t vec_i = SIMD_I32_LEN; // how many doubles in each vec

  // 1. Average of all returns.
  double avg = 0.0;
  for (i = 0; i < _n; i++) avg += _data[i];
  avg /= _n;

  // 2. Squared Differences: For each return, subtract the mean and square it.
  double sum_sq_diff_v = 0.0; // create zeroed vector of 4 doubles 

  for (i = 0; i <= _n-vec_i; i += vec_i)
  {
    // Create vector with next 4 returns
    SIMD_INT_T ret_v = SIMD_I32_LOADU(&_data[i]);

    // Create vector with each double set to avg
    SIMD_INT_T avg_v = SIMD_I32_SET1(avg);

    // Create vector with avg subtracted from returns vectors
    SIMD_INT_T diff_v = SIMD_I32_SUB(ret_v, avg_v);

    // sum_sq_diff += diff * diff; (but for vector)
    sum_sq_diff_v = SIMD_I32_ADD(sum_sq_diff_v, SIMD_I32_MUL(diff_v, diff_v));
  }
  // Add the partially summed vector doubles together 
  if (vec_i == 4)
  {
    int32_t temp_v[4];
    SIMD_I32_STOREU((SIMD_INT_T*)temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3];
  }
  else if (vec_i == 8) {
    int32_t temp_v[8];
    SIMD_I32_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3] + 
                  temp_v[4] + temp_v[5] + temp_v[6] + temp_v[7];
  }
  else if (vec_i == 16) {
    int32_t temp_v[16];
    SIMD_I32_STOREU(temp_v, sum_sq_diff_v);
    sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3] + 
                  temp_v[4] + temp_v[5] + temp_v[6] + temp_v[7] +
                  temp_v[8] + temp_v[9] + temp_v[10] + temp_v[11] + 
                  temp_v[12] + temp_v[13] + temp_v[14] + temp_v[15];
  }
  else {
    //TODO: error log
    return 0.0;
  }
  

  // We also need to handle remainders 
  // since if _data isn't a multiple of vec_i, 
  // last few values will be excluded from loop 
  // Finish loop regularly
  for(; i < _n; i++) {
    double diff = _data[i] - avg;
    sum_sq_diff += diff * diff;
  }

  // 3. Sum & Divide: Sum these squares, divide by N-1 (Bessel’s correction).
  // 4. Square Root: Take the square root of divided sum to get volatility.
  return sqrt(sum_sq_diff /= (_n-1));

}

#endif // SIMD_I32_LEN
#endif // HAS_SIMD
