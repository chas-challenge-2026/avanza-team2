#include "volatility.h"
#include "time_utils.h"
#include "test_data.h"

#include <stdio.h>
#include <stdlib.h>

#define SAMPLE_ARR_SIZE 252*1024*1024

void risk_test_volatility(void)
{
  /* Sample array inputs */
  const size_t arr_n = SAMPLE_ARR_SIZE; // Array size
  uint64_t time_start;
  uint64_t time_end;
  uint64_t time_ns;
  double time_s;

  /* Double test*/
  double arr_base_start_d = 100.0;     // Initial base value
  double arr_base_step_d = 0.01;       // Increase base by this amount each iteration
  double arr_noise_magnitude_d = 10.0; // Randomize +/- this amount
  double volatility_d;

  double* arr_d = calloc(1, (arr_n * sizeof(double)));
  if (!arr_d)
    exit(1);

  printf("Generating an array of %ld doubles\n", arr_n);
  test_gen_sample_arr_double(arr_d, 
                      arr_n, 
                      arr_base_start_d, 
                      arr_base_step_d, 
                      arr_noise_magnitude_d);
  printf("First result: %lf\n", arr_d[0]);
  printf("Last result (index %ld): %lf\n",arr_n-1, arr_d[arr_n-1]);
  // for (size_t i = 0; i < arr_n; i++)
  //   printf("%lf,", arr[i]);

  /* Run and time calculations on array */

  time_start = system_monotonic_ns(); 
  volatility_d = risk_calc_volatility_double(arr_d, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_double took %lf seconds\n", time_s);
  printf("Volatility: %lf\n", volatility_d);

  time_start = system_monotonic_ns(); 
  volatility_d = risk_calc_volatility_double_simd(arr_d, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_double_simd took %lf seconds\n", time_s);
  printf("Volatility: %lf\n", volatility_d);

  free(arr_d);

  /* Float test */
  float arr_base_start_f = 100.0;     // Initial base value
  float arr_base_step_f = 0.01;       // Increase base by this amount each iteration
  float arr_noise_magnitude_f = 10.0; // Randomize +/- this amount
  float volatility_f;
  float* arr_f = calloc(1, (arr_n * sizeof(float)));
  if (!arr_f)
    exit(1);
  printf("Generating an array of %ld float\n", arr_n);
  test_gen_sample_arr_float(arr_f, 
                      arr_n, 
                      arr_base_start_f, 
                      arr_base_step_f, 
                      arr_noise_magnitude_f);
  printf("First result: %lf\n", arr_f[0]);
  printf("Last result (index %ld): %lf\n",arr_n-1, arr_f[arr_n-1]);
    /* Run and time calculations on array */

  time_start = system_monotonic_ns(); 
  volatility_f = risk_calc_volatility_float(arr_f, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_float took %lf seconds\n", time_s);
  printf("Volatility: %f\n", volatility_f);

  time_start = system_monotonic_ns(); 
  volatility_f = risk_calc_volatility_float_simd(arr_f, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_float_simd took %lf seconds\n", time_s);
  printf("Volatility: %f\n", volatility_f);

  free(arr_f);

}

int main(void) 
{
  risk_test_volatility();
  
  return 0;
}

