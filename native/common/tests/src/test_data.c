#include "test_data.h"

#include <time.h>


void test_gen_sample_arr_double(double *_arr, 
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

void test_gen_sample_arr_float(float *_arr, 
  const size_t _n, 
  const float _base_start, 
  const float _base_step, 
  const float _noise_magnitude) 
{
  // Seed random number generator once (typically done at program start)
  srand(time(NULL));

  for (size_t i = 0; i < _n; i++) {
    // Calculate current base value: increases by base_step each iteration
    float current_base = _base_start + (i * _base_step);

    // Generate random noise in range [-noise_magnitude, +noise_magnitude]
    // rand() returns [0, RAND_MAX]. Normalize to [0, 1], scale to [0, 2*noise_magnitude], shift to [-noise_magnitude, +noise_magnitude]
    float normalized_rand = (float)rand() / RAND_MAX;
    float noise = (normalized_rand * 2.0 - 1.0) * _noise_magnitude;

    // Assign value
    _arr[i] = current_base + noise;

  }
}

void test_gen_sample_arr_int32(int32_t *_arr, 
  const size_t _n, 
  const int32_t _base_start, 
  const int32_t _base_step, 
  const int32_t _noise_magnitude) 
{
  // Seed random number generator once (typically done at program start)
  srand(time(NULL));

  for (size_t i = 0; i < _n; i++) {
    // Calculate current base value: increases by base_step each iteration
    int32_t current_base = _base_start + (i * _base_step);

    // Generate random noise in range [-noise_magnitude, +noise_magnitude]
    // rand() returns [0, RAND_MAX]. Normalize to [0, 1], scale to [0, 2*noise_magnitude], shift to [-noise_magnitude, +noise_magnitude]
    int32_t normalized_rand = (int32_t)rand() / RAND_MAX;
    int32_t noise = (normalized_rand * 2.0 - 1.0) * _noise_magnitude;

    // Assign value
    _arr[i] = current_base + noise;

  }
}

void test_gen_sample_arr_int8(int8_t *_arr, 
  const size_t _n, 
  const int8_t _base_start, 
  const int8_t _base_step, 
  const int8_t _noise_magnitude) 
{
  // Seed random number generator once (typically done at program start)
  srand(time(NULL));

  for (size_t i = 0; i < _n; i++) {
    // Calculate current base value: increases by base_step each iteration
    int8_t current_base = _base_start + (i * _base_step);

    // Generate random noise in range [-noise_magnitude, +noise_magnitude]
    // rand() returns [0, RAND_MAX]. Normalize to [0, 1], scale to [0, 2*noise_magnitude], shift to [-noise_magnitude, +noise_magnitude]
    int8_t normalized_rand = (int8_t)rand() / RAND_MAX;
    int8_t noise = (normalized_rand * 2.0 - 1.0) * _noise_magnitude;

    // Assign value
    _arr[i] = current_base + noise;

  }
}
