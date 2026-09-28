#ifndef __TIME_UTILS_H__
#define __TIME_UTILS_H__

#define _POSIX_C_SOURCE 199309L

#include <time.h>
#include <stdint.h>

#define ISO_DATE_STR_FULL "%04d-%02d-%02dT%02d:%02d:%02d%c%02d:%02d"
#define ISO_DATE_STR_FULL_LEN 20
#define ISO_DATE_STR_MIN  "%04d-%02d-%02dT%02d:%02d"
#define ISO_DATE_STR_MIN_LEN 16
#define ISO_DATE_STR_DAY  "%04d-%02d-%02d"
#define ISO_DATE_STR_DAY_LEN 10

/* chatte generated custom timegm for non-posix platforms */
#ifndef timegm
time_t timegm(struct tm* _tm);
#endif /* timegm */

/** Monotonic timer in ms */
uint64_t system_monotonic_ms(void);

/** Monotonic timer in us */
uint64_t system_monotonic_us(void);

/** Monotonic timer in ns */
uint64_t system_monotonic_ns(void);

/* Helper for parsing iso formatted datetime string to time_t epoch
 * Format: "1970-01-01T00:00:00+00:00" */
time_t time_parse_iso_date_full_str_to_epoch(const char* _time_str);

/* Helper for parsing iso8601 formatted datetime string to time_t epoch
 * Format: 1970-01-01T00:00 */
time_t time_parse_iso_date_min_str_to_epoch(const char* _time_str);

/* Helper for parsing iso8601 formatted datetime string to time_t epoch
 * Format: "1970-01-01" */
time_t time_parse_iso_date_day_str_to_epoch(const char* _time_str);

#endif // __TIME_UTILS_H__ 
