#ifndef __RATES_HANDLER_H__
#define __RATES_HANDLER_H__

#include <time.h>

typedef enum 
{
  OneMonth = 21,
  ThreeMonth = 63,
  SixMonth = 126,
  // OneYear = 256,
  None = -1,

} TBillType;

typedef struct
{
  time_t    date;
  TBillType type;
  double    value;

} TBillRate;

/* Returns Treasury Bill rate based on type chosen 
 * Fetches rate from riskbanken API
 * Caches and reuses response */
TBillRate rates_handler_tbill_get_latest(TBillType _RT);


#endif // __RATES_HANDLER_H__
