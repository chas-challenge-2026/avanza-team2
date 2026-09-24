#ifndef __RATES_HANDLER_H__
#define __RATES_HANDLER_H__

#ifdef __cplusplus
extern "C" {
#endif

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

/* Procures interest rate in decimal form (i.e 0.036 for 3.6%) based on type chosen 
 * Fetches rate from riskbanken API, caches and reuses response 
 * Riksbank API rate limit: 5 Requests/min and 1000 requests/day
 * Returns: 0 for success, 429 for request rate limit hit, else misc errors
 * NOTE: all rates are in SEK */
int rates_handler_get_latest(Rate* _R, RateType _Type);

/* Procures overnight riskfree interest rate in decimal form (i.e 0.036 for 3.6%) 
 * Fetches rate from riskbanken API, caches and reuses response 
 * Riksbank API rate limit: 5 Requests/min and 1000 requests/day
 * Returns: 0 for success, 429 for request rate limit hit, else misc errors
 * NOTE: all rates are in SEK 
 * Simpler version without timestamp and only uses SWESTR series */
int rates_handler_get_latest_swestr(double* _rate);

/* Returns the equivalent SeriesId string per T-Bill type for riksbank API
 * Returns NULL on None or unknown type */
const char* rates_handler_get_rbapi_seriesid(RateType _Type);

#ifdef __cplusplus
}
#endif

#endif // __RATES_HANDLER_H__
