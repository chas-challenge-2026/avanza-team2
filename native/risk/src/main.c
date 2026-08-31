#include "risk.h"
#include "time_utils.h"

#include <stdio.h>
#include <stdlib.h>

// #define SAMPLE_ARR_SIZE 252


int main(void) 
{
  /* Sample array inputs */
  const size_t arr_n = 252*1024;          // Array size
  double arr_base_start = 100.0;     // Initial base value
  double arr_base_step = 0.01;       // Increase base by this amount each iteration
  double arr_noise_magnitude = 10.0; // Randomize +/- this amount

  double* arr = calloc(1, (arr_n * sizeof(double)));
  if (!arr)
    exit(1);

  printf("Generating an array of %ld results\n", arr_n);
  generate_sample_arr(arr, 
                      arr_n, 
                      arr_base_start, 
                      arr_base_step, 
                      arr_noise_magnitude);
  printf("First result: %lf\n", arr[0]);
  printf("Last result (index %ld): %lf\n",arr_n-1, arr[arr_n-1]);
  // for (size_t i = 0; i < arr_n; i++)
  //   printf("%lf,", arr[i]);

  /* Run calculations on array */
  uint64_t time_start;
  uint64_t time_end;
  uint64_t time_ns;
  double volatility;
  double time_s;

  time_start = system_monotonic_ns(); 
  volatility = risk_calc_volatility_dbl_simple(arr, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_dbl_simple took %lf seconds\n", time_s);
  printf("Volatility: %lf\n", volatility);

  time_start = system_monotonic_ns(); 
  volatility = risk_calc_volatility_dbl_simd(arr, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_dbl_simd took %lf seconds\n", time_s);
  printf("Volatility: %lf\n", volatility);

  free(arr);
  
  return 0;
}

