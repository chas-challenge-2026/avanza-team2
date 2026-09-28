#ifndef __DATA_UTILS_H__
#define __DATA_UTILS_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "stdlib.h"

/* Converts cumulative asset values to decimal returns */
int data_convert_values_to_returns(const double* _values_in, double* _returns_out, size_t _n);

#ifdef __cplusplus
} // extern "C"
#endif

#endif // __DATA_UTILS_H__
