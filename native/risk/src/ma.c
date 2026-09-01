#include "ma.h"

double* risk_calc_sma_double(const double* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  size_t i;

  // Allocate SMA array. 
  size_t result_size = _n - _window + 1;
  double* sma_result = malloc(result_size * sizeof(double));
  if (!sma_result)
    return NULL;
  //TODO: Err no mem
  
  // 1. Calculate sum of the first _window elements in _data
  double current_sum = 0.0;
  for (i = 0; i < _window; i++)
    current_sum += _data[i];
  
  // 2. Store the first average value, which starts at index _window in _data
  sma_result[0] = current_sum / _window;

  // 3. Calculate subsequent SMA results using sliding window
  for (i = _window; i < _n; i++) {
    // Subtract the data value leaving the window and add the one entering it
    current_sum = current_sum - _data[i - _window] + _data[i];
    // Divide sum and store
    sma_result[i - _window + 1] = current_sum / _window;
  }
  
  return sma_result;
}

double* risk_calc_wma_double(const double* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  size_t i, j, weight;
  double weighted_sum;

  // Allocate WMA array. 
  size_t result_size = _n - _window + 1;
  double* wma_result = malloc(result_size * sizeof(double));
  if (!wma_result)
    return NULL;
  //TODO: Err no mem

  // 1. Pre-compute sum of weights based on window size
  // double weights_sum = 0.0;
  // for (i = 0; i <= _window; i++)
  //   weights_sum += i;
  double weights_sum = (double)_window * ((double)_window + 1.0) / 2.0;

  // 2. Calculate each result's WMA for each window position 
  // (This could def get faster using SIMD)
  for (i = 0; i < result_size; i++) {
    weighted_sum = 0.0;
    
    for (j = 0; j < _window; j++) {
      weight = j + 1;
      weighted_sum += _data[i + j] * weight;

    }
    // 3. Normalize the weighted sum; divide it by sum of weights
    wma_result[i] = weighted_sum / weights_sum;
  }
  
  return wma_result;
}

double* risk_calc_ema_double(const double* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  // Allocate EMA array. 
  size_t result_size = _n;
  double* ema_result = malloc(result_size * sizeof(double));
  if (!ema_result)
    return NULL;
  //TODO: Err no mem

  // 1. Calculate alpha (smoothing factor)
  double alpha = 2.0 / (_window + 1);

  // 2. Init ema with first data point
  double ema = _data[0];
  ema_result[0] = ema;

  // 3. Calculate EMA for each subsequent data point
  for (size_t i = 1; i < _n; i++) {
    // Recursive formula: y_t = alpha * x_t + (1 - alpha) * y_{t-1}
    ema = alpha * _data[i] + (1.0 - alpha) * ema;
    ema_result[i] = ema;
  }
  
  return ema_result;
}

float* risk_calc_sma_float(const float* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  size_t i;

  // Allocate SMA array. 
  size_t result_size = _n - _window + 1;
  float* sma_result = malloc(result_size * sizeof(float));
  if (!sma_result)
    return NULL;
  //TODO: Err no mem
  
  // 1. Calculate sum of the first _window elements in _data
  float current_sum = 0.0;
  for (i = 0; i < _window; i++)
    current_sum += _data[i];
  
  // 2. Store the first average value, which starts at index _window in _data
  sma_result[0] = current_sum / _window;

  // 3. Calculate subsequent SMA results using sliding window
  for (i = _window; i < _n; i++) {
    // Subtract the data value leaving the window and add the one entering it
    current_sum = current_sum - _data[i - _window] + _data[i];
    // Divide sum and store
    sma_result[i - _window + 1] = current_sum / _window;
  }
  
  return sma_result;
}

float* risk_calc_wma_float(const float* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  size_t i, j, weight;
  float weighted_sum;

  // Allocate WMA array. 
  size_t result_size = _n - _window + 1;
  float* wma_result = malloc(result_size * sizeof(float));
  if (!wma_result)
    return NULL;
  //TODO: Err no mem

  // 1. Pre-compute sum of weights based on window size
  // float weights_sum = 0.0;
  // for (i = 0; i <= _window; i++)
  //   weights_sum += i;
  float weights_sum = (float)_window * ((float)_window + 1.0) / 2.0;

  // 2. Calculate each result's WMA for each window position 
  // (This could def get faster using SIMD)
  for (i = 0; i < result_size; i++) {
    weighted_sum = 0.0;
    
    for (j = 0; j < _window; j++) {
      weight = j + 1;
      weighted_sum += _data[i + j] * weight;

    }
    // 3. Normalize the weighted sum; divide it by sum of weights
    wma_result[i] = weighted_sum / weights_sum;
  }
  
  return wma_result;
}

float* risk_calc_ema_float(const float* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  // Allocate EMA array. 
  size_t result_size = _n;
  float* ema_result = malloc(result_size * sizeof(float));
  if (!ema_result)
    return NULL;
  //TODO: Err no mem

  // 1. Calculate alpha (smoothing factor)
  float alpha = 2.0 / (_window + 1);

  // 2. Init ema with first data point
  float ema = _data[0];
  ema_result[0] = ema;

  // 3. Calculate EMA for each subsequent data point
  for (size_t i = 1; i < _n; i++) {
    // Recursive formula: y_t = alpha * x_t + (1 - alpha) * y_{t-1}
    ema = alpha * _data[i] + (1.0 - alpha) * ema;
    ema_result[i] = ema;
  }
  
  return ema_result;
}


int32_t* risk_calc_sma_int32_t(const int32_t* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  size_t i;

  // Allocate SMA array. 
  size_t result_size = _n - _window + 1;
  int32_t* sma_result = malloc(result_size * sizeof(int32_t));
  if (!sma_result)
    return NULL;
  //TODO: Err no mem
  
  // 1. Calculate sum of the first _window elements in _data
  int32_t current_sum = 0.0;
  for (i = 0; i < _window; i++)
    current_sum += _data[i];
  
  // 2. Store the first average value, which starts at index _window in _data
  sma_result[0] = current_sum / _window;

  // 3. Calculate subsequent SMA results using sliding window
  for (i = _window; i < _n; i++) {
    // Subtract the data value leaving the window and add the one entering it
    current_sum = current_sum - _data[i - _window] + _data[i];
    // Divide sum and store
    sma_result[i - _window + 1] = current_sum / _window;
  }
  
  return sma_result;
}

int32_t* risk_calc_wma_int32_t(const int32_t* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  size_t i, j, weight;
  int32_t weighted_sum;

  // Allocate WMA array. 
  size_t result_size = _n - _window + 1;
  int32_t* wma_result = malloc(result_size * sizeof(int32_t));
  if (!wma_result)
    return NULL;
  //TODO: Err no mem

  // 1. Pre-compute sum of weights based on window size
  // int32_t weights_sum = 0.0;
  // for (i = 0; i <= _window; i++)
  //   weights_sum += i;
  int32_t weights_sum = (int32_t)_window * ((int32_t)_window + 1.0) / 2.0;

  // 2. Calculate each result's WMA for each window position 
  // (This could def get faster using SIMD)
  for (i = 0; i < result_size; i++) {
    weighted_sum = 0.0;
    
    for (j = 0; j < _window; j++) {
      weight = j + 1;
      weighted_sum += _data[i + j] * weight;

    }
    // 3. Normalize the weighted sum; divide it by sum of weights
    wma_result[i] = weighted_sum / weights_sum;
  }
  
  return wma_result;
}

int32_t* risk_calc_ema_int32_t(const int32_t* _data, size_t _n, size_t _window)
{
  if (!_data || _window < 1 || _window > _n)
    return NULL;

  // Allocate EMA array. 
  size_t result_size = _n;
  int32_t* ema_result = malloc(result_size * sizeof(int32_t));
  if (!ema_result)
    return NULL;
  //TODO: Err no mem

  // 1. Calculate alpha (smoothing factor)
  int32_t alpha = 2.0 / (_window + 1);

  // 2. Init ema with first data point
  int32_t ema = _data[0];
  ema_result[0] = ema;

  // 3. Calculate EMA for each subsequent data point
  for (size_t i = 1; i < _n; i++) {
    // Recursive formula: y_t = alpha * x_t + (1 - alpha) * y_{t-1}
    ema = alpha * _data[i] + (1.0 - alpha) * ema;
    ema_result[i] = ema;
  }
  
  return ema_result;
}

/***************************** SIMD VERSIONS *********************************/

#if defined(HAS_SIMD) && defined(SIMD_D_DIV)

/* LLM slop, will oversee later */
double* risk_calc_wma_double_simd(const double* _data, size_t _n, size_t _window) {
  if (!_data || _window < 1 || _window > _n) return NULL;

  size_t result_size = _n - _window + 1;
  double* wma_result = malloc(result_size * sizeof(double));
  if (!wma_result) return NULL;

  // Precompute weights and weight sum
  double weights[SIMD_D_LEN]; // Max window size must fit in SIMD register
  double weights_sum = 0.0;
  for (size_t j = 0; j < _window; j++) {
    weights[j] = j + 1; // Weights: 1, 2, ..., window
    weights_sum += weights[j];
  }

  // Load weights into SIMD vector
  SIMD_DOUBLE_T weights_v = SIMD_D_LOADU(weights);

  // Process output indices in chunks of SIMD_D_LEN
  size_t vec_i = SIMD_D_LEN;
  for (size_t i = 0; i + vec_i <= result_size; i += vec_i) {
    // Initialize weighted sums for each lane to 0
    SIMD_DOUBLE_T weighted_sums_v = SIMD_D_SET1(0.0);

    // Compute weighted sum for each window in the chunk
    for (size_t j = 0; j < _window; j++) {
      // Load data points for all lanes in the chunk
      SIMD_DOUBLE_T data_v = SIMD_D_LOADU(&_data[i + j]);
      
      // Multiply by weight and accumulate
      double weight = weights[j];
      SIMD_DOUBLE_T weight_v = SIMD_D_SET1(weight);
      weighted_sums_v = SIMD_D_ADD(weighted_sums_v, SIMD_D_MUL(data_v, weight_v));
    }

    // Normalize by dividing by weights_sum
    SIMD_DOUBLE_T weights_sum_v = SIMD_D_SET1(weights_sum);
    SIMD_DOUBLE_T wma_v = SIMD_D_DIV(weighted_sums_v, weights_sum_v);

    // Store results
    SIMD_D_STOREU(&wma_result[i], wma_v);
  }

  // Handle remainder (if result_size is not a multiple of vec_i)
  for (size_t i = (result_size / vec_i) * vec_i; i < result_size; i++) {
    double weighted_sum = 0.0;
    for (size_t j = 0; j < _window; j++) {
      weighted_sum += _data[i + j] * weights[j];
    }
    wma_result[i] = weighted_sum / weights_sum;
  }

  return wma_result;
}

/* Failed experiment, each EMA value needs to be chained from the previous one,
 * so using parallellism will break the chain. 
 * Could potentially unroll each vector member in the loop but prob not worth it. */
// double* risk_calc_ema_double_simd(const double* _data, size_t _n, size_t _window)
// {
//   if (!_data || _window < 1 || _window > _n)
//     return NULL;
//
//   // Allocate EMA array. 
//   size_t result_size = _n;
//   double* ema_result = malloc(result_size * sizeof(double));
//   if (!ema_result)
//     return NULL;
//   //TODO: Err no mem
//
//   // 1. Calculate alpha (smoothing factor)
//   double alpha = 2.0 / (_window + 1);
//
//   // 2. Init ema with first data point
//   double ema = _data[0];
//   ema_result[0] = ema;
//
//   // 3. Calculate EMA for each subsequent data point using simd vectors
//   size_t i, j;
//   size_t vec_i = SIMD_D_LEN; // how many doubles in each vec
//   double temp_res[SIMD_D_LEN];
//   SIMD_DOUBLE_T alpha_v = SIMD_D_SET1(alpha);
//   SIMD_DOUBLE_T alpha_norm_v = SIMD_D_SET1(1.0-alpha);
//   SIMD_DOUBLE_T ema_v = SIMD_D_SET1(ema);
//   SIMD_DOUBLE_T data_v;
//   for (i = 1; i < _n-vec_i; i += vec_i) {
//     data_v = SIMD_D_LOADU(&_data[i]);
//
//     // Recursive formula: y_t = alpha * x_t + (1 - alpha) * y_{t-1}
//     SIMD_D_MUL(SIMD_D_ADD(SIMD_D_MUL(alpha_v, data_v), alpha_norm_v), ema_v);
//
//     // ema = alpha * _data[i] + (1.0 - alpha) * ema;
//     // ema_result[i] = ema;
//
//     /* Store the ema vector values in the results array */
//     SIMD_D_STOREU(temp_res, ema_v);
//     for (j = 0; j < vec_i; j++)
//       ema_result[i+j] = temp_res[j];
//   }
//
//   // Handle remainders if _data isn't a multiple of vec_i, 
//   for(; i < _n; i++) {
//     ema = alpha * _data[i] + (1.0 - alpha) * ema;
//     ema_result[i] = ema;
//   }
//   
//   return ema_result;
// }

#endif
