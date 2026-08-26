#include "risk.h"

#include <stdio.h>

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

#if HAS_SIMD
double risk_calc_volatility_dbl_simd(const double* _returns, size_t _n)
{
  // Won't matter if dereffed values aren't inited etc. but guards a lil
  if (!_returns || _n < 2) return 0.0;

  printf("Calculating volatility using SIMD level: %s\n", SIMD_LEVEL);

  size_t i;
  double sum_sq_diff;
  size_t vec_i = SIMD_D_LEN; // how many doubles in each vec

  // 1. Average of all returns.
  double avg = 0.0;
  for (i = 0; i < _n; i++) avg += _returns[i];
  avg /= _n;

  // 2. Squared Differences: For each return, subtract the mean and square it.
  SIMD_DOUBLE_T sum_sq_diff_v = SIMD_D_ZERO(); // create zeroed vector of 4 doubles 

  for (i = 0; i <= _n-vec_i; i += vec_i)
  {
    // Create vector with next 4 returns
    SIMD_DOUBLE_T ret_v = SIMD_D_LOADU(&_returns[i]);

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
  // since if _returns isn't a multiple of vec_i, 
  // last few values will be excluded from loop 
  // Finish loop regularly
  for(; i < _n; i++) {
    double diff = _returns[i] - avg;
    sum_sq_diff += diff * diff;
  }

  // 3. Sum & Divide: Sum these squares, divide by N-1 (Bessel’s correction).
  // 4. Square Root: Take the square root of divided sum to get volatility.
  return sqrt(sum_sq_diff /= (_n-1));

}

double risk_calc_volatility_dbl_avx256(const double* _returns, size_t _n)
{
  // Won't matter if dereffed values aren't inited etc. but guards a lil
  if (!_returns || _n < 2) return 0.0;

  printf("Calculating volatility using AVX256\n");

  size_t i;
  double sum_sq_diff;

  // 1. Average of all returns.
  double avg = 0.0;
  for (i = 0; i < _n; i++) avg += _returns[i];
  avg /= _n;

  // 2. Squared Differences: For each return, subtract the mean and square it.
  __m256d sum_sq_diff_v = _mm256_setzero_pd(); // create zeroed vector of 4 doubles 

  for (i = 0; i <= _n-4; i += 4) // 4 less iterations bcuz simd vector!
  {
    // Create vector with next 4 returns
    __m256d ret_v = _mm256_loadu_pd(&_returns[i]);

    // Create vector with each double set to avg
    __m256d avg_v = _mm256_set1_pd(avg);

    // Create vector with avg subtracted from returns vectors
    __m256d diff_v = _mm256_sub_pd(ret_v, avg_v);

    // sum_sq_diff += diff * diff; (but for vector)
    sum_sq_diff_v = _mm256_add_pd(sum_sq_diff_v, _mm256_mul_pd(diff_v, diff_v));
  }
  // Add the four partially summed vector doubles together 
  double temp_v[4];
  _mm256_storeu_pd(temp_v, sum_sq_diff_v);
  sum_sq_diff = temp_v[0] + temp_v[1] + temp_v[2] + temp_v[3];

  // We also need to handle remainders 
  // since if _returns isn't a multiple of 4, last few values will be excluded from loop 
  // Finish loop regularly
  for(; i < _n; i++) {
    double diff = _returns[i] - avg;
    sum_sq_diff += diff * diff;
  }

  // 3. Sum & Divide: Sum these squares, divide by N-1 (Bessel’s correction).
  // 4. Square Root: Take the square root of divided sum to get volatility.
  return sqrt(sum_sq_diff /= (_n-1));

}

#endif

double risk_calc_volatility_dbl_simple(double* _returns, int _n) 
{
  if (!_returns || _n <= 1) return 0.0;

  // Step 1: Calculate mean
  double sum = 0.0;
  for (int i = 0; i < _n; i++) {
    sum += _returns[i];
  }
  double mean = sum / _n;

  // Step 2: Calculate sum of squared differences from the mean
  double sum_sq_diff = 0.0;
  for (int i = 0; i < _n; i++) {
    double diff = _returns[i] - mean;
    sum_sq_diff += diff * diff;
  }

  // Step 3: Calculate sample standard deviation
  return sqrt(sum_sq_diff / (_n - 1));
}

void generate_sample_arr(double *_arr, 
  const size_t _n, 
  const double _base_start, 
  const double _base_step, 
  const double _noise_magnitude) 
{
  // Seed random number generator once (typically done at program start)
  srand(time(NULL));

  for (size_t i = 0; i < _n; i++) {
    // Calculate current base value: increases by base_step each iteration
    double current_base = _base_start + (i * _base_step);

    // Generate random noise in range [-noise_magnitude, +noise_magnitude]
    // rand() returns [0, RAND_MAX]. Normalize to [0, 1], scale to [0, 2*noise_magnitude], shift to [-noise_magnitude, +noise_magnitude]
    double normalized_rand = (double)rand() / RAND_MAX;
    double noise = (normalized_rand * 2.0 - 1.0) * _noise_magnitude;

    // Assign value
    _arr[i] = current_base + noise;

  }
}

