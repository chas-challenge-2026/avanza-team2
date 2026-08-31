#ifndef __TIME_UTILS_H__
#define __TIME_UTILS_H__

#include <time.h>
#include <stdint.h>


/** Robin's Monotonic timer in ms */
uint64_t system_monotonic_ms(void);

/** Robin's Monotonic timer in us */
uint64_t system_monotonic_us(void);

/** Robin's Monotonic timer in ns */
uint64_t system_monotonic_ns(void);

#endif // __TIME_UTILS_H__ 
