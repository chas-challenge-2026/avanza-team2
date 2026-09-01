#ifndef __TIME_UTILS_H__
#define __TIME_UTILS_H__

#include <time.h>
#include <stdint.h>


/** Monotonic timer in ms */
uint64_t system_monotonic_ms(void);

/** Monotonic timer in us */
uint64_t system_monotonic_us(void);

/** Monotonic timer in ns */
uint64_t system_monotonic_ns(void);

#endif // __TIME_UTILS_H__ 
