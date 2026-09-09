#ifndef __RATES_HANDLER_H__
#define __RATES_HANDLER_H__

#include <time.h>

typedef enum 
{
  Swestr     = 1,    // Overnight cash benchmark, best for sharpe ratio
  OneMonth   = 21,   // Swedish T-Bill 1 Month
  ThreeMonth = 63,   // Swedish T-Bill 3 Month
  SixMonth   = 126,  // Swedish T-Bill 6 Month
  TwoYear    = 512,  // Swedish Gov Bond 2 Years
  FiveYear   = 1280, // Swedish Gov Bond 5 Years
  TenYear    = 2560, // Swedish Gov Bond 10 Years
  None       = -1,

} RateType;

typedef struct
{
  double   value;
  time_t   date;
  RateType type;

} Rate;

/* Procures interest rate based on type chosen 
 * Fetches rate from riskbanken API
 * Caches and reuses response */
int rates_handler_get_latest(Rate* _R, RateType _Type);

/* Returns the equivalent SeriesId string per T-Bill type for riksbank API
 * Returns NULL on None or unknown type */
const char* rates_handler_get_rbapi_seriesid(RateType _Type);

#endif // __RATES_HANDLER_H__
