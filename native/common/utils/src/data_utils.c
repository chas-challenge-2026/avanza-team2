#include "data_utils.h"

int data_convert_values_to_returns(const double* _values_in, double* _returns_out, size_t _n)
{
  if (!_values_in || !_returns_out || _n < 2)
    return 1;

  double prev_val = 0.0;
  for (size_t i = 1; i < _n; i++)
  {
    prev_val = _values_in[i-1];
    if (prev_val == 0) // No division by zero, this breaks the chain
      _returns_out[i] = 0.0;
    else 
      _returns_out[i] = (_values_in[i] - prev_val) / prev_val;
  }

  return 0;
}

