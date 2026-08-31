#include "volatility.h"
#include "time_utils.h"
#include "test_data.h"
#include "ma.h"

#include <stdio.h>
#include <stdlib.h>

// #define SAMPLE_ARR_SIZE 252


int main(void) 
{
  /* Sample array inputs */
  const size_t arr_n = 256*1024;     // Array size
  double arr_base_start = 100.0;     // Initial base value
  double arr_base_step = 0.01;       // Increase base by this amount each iteration
  double arr_noise_magnitude = 10.0; // Randomize +/- this amount

  double* arr = calloc(1, (arr_n * sizeof(double)));
  if (!arr)
    exit(1);

  printf("Generating an array of %ld results\n", arr_n);
  test_gen_sample_arr_double(arr, 
                      arr_n, 
                      arr_base_start, 
                      arr_base_step, 
                      arr_noise_magnitude);
  printf("First result: %lf\n", arr[0]);
  printf("Last result (index %ld): %lf\n",arr_n-1, arr[arr_n-1]);


  /* Run and time volatility calculations */
  uint64_t time_start;
  uint64_t time_end;
  uint64_t time_ns;
  double time_s;

  double volatility_result;

  time_start = system_monotonic_ns(); 
  volatility_result = risk_calc_volatility_double(arr, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_double took %lf seconds\n", time_s);
  printf("Volatility: %lf\n", volatility_result);

  time_start = system_monotonic_ns(); 
  volatility_result = risk_calc_volatility_double_simd(arr, arr_n);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_volatility_double_simd took %lf seconds\n", time_s);
  printf("Volatility: %lf\n", volatility_result);


  /* Run and time SMA calculations */
  size_t ma_window = 20;
  time_start = system_monotonic_ns(); 
  double* ma_result = risk_calc_sma_double(arr, arr_n, ma_window);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_sma_double took %lf seconds\n", time_s);
  if (!ma_result)
    exit(1);

  printf("\n--SMA results--\n");
  for (size_t i = 0; i < ma_window; i++)
  {
    size_t resu_i = arr_n - (ma_window - i);
    printf("index %zu/%zu = ", resu_i, arr_n-1);
    printf("%zu - res: %lf; SMA: %lf\n", i+1, arr[resu_i], ma_result[resu_i-ma_window+1]);
  }

  free(ma_result);

  /* Run and time WMA calculations */
  time_start = system_monotonic_ns(); 
  double* wma_result = risk_calc_wma_double(arr, arr_n, ma_window);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;

  printf("risk_calc_wma_double took %lf seconds\n", time_s);
  if (!ma_result)
    exit(1);

  printf("\n--WMA results--\n");
  for (size_t i = 0; i < ma_window; i++)
  {
    size_t resu_i = arr_n - (ma_window - i);
    printf("index %zu/%zu = ", resu_i, arr_n-1);
    printf("%zu - res: %lf; WMA: %lf\n", i+1, arr[resu_i], wma_result[resu_i-ma_window+1]);
  }

  free(wma_result);

  /* Run and time EMA calculations */
  time_start = system_monotonic_ns(); 
  double* ema_result = risk_calc_ema_double(arr, arr_n, ma_window);
  time_end = system_monotonic_ns(); 
  time_ns = time_end - time_start;
  time_s = (double)time_ns / 1e9;
  printf("risk_calc_ema_double took %lf seconds\n", time_s);

  if (!ma_result)
    exit(1);

  printf("\n--EMA results--\n");
  for (size_t i = 0; i < ma_window; i++)
  {
    size_t resu_i = arr_n - (ma_window - i);
    printf("index %zu/%zu = ", resu_i, arr_n-1);
    printf("%zu - res: %lf; EMA: %lf\n", i+1, arr[resu_i], wma_result[resu_i-ma_window+1]);
  }

  free(ema_result);


  free(arr);
  
  return 0;
}

