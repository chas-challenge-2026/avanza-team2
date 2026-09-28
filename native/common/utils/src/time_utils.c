#include "time_utils.h"
#include <stdio.h>

#ifndef timegm
time_t timegm(struct tm* _tm)
{
  static const int days_in_month[12] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  int              year              = _tm->tm_year + 1900;
  int              month             = _tm->tm_mon;
  int              day = _tm->tm_mday - 1; /* struct tm days start from 1, Unix epoch from 0 */

  /* Calculate days before current year */
  int days =
      (year - 1970) * 365 + ((year - 1969) / 4) - ((year - 1901) / 100) + ((year - 1601) / 400);
  /* Add days for previous months this year */
  for (int i = 0; i < month; ++i)
    days += days_in_month[i];
  /* Leap year adjustment */
  if (month > 1 && ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0))
    days += 1;
  /* Add current day */
  days += day;
  /* Convert to seconds */
  return (time_t)((((days * 24 + _tm->tm_hour) * 60 + _tm->tm_min) * 60) + _tm->tm_sec);
}
#endif /* timegm */

uint64_t system_monotonic_ms(void) {
    struct timespec spec;
    clock_gettime(CLOCK_MONOTONIC, &spec);
    return (uint64_t)spec.tv_sec * 1000ULL + (uint64_t)spec.tv_nsec / 1000000ULL;
}

uint64_t system_monotonic_us(void) {
  struct timespec spec;
  clock_gettime(CLOCK_MONOTONIC, &spec);
  return (uint64_t)spec.tv_sec * 1000000ULL + (uint64_t)spec.tv_nsec / 1000ULL;
}

uint64_t system_monotonic_ns(void) {
    struct timespec spec;
    clock_gettime(CLOCK_MONOTONIC, &spec);
    return (uint64_t)spec.tv_sec * 1000000000ULL + (uint64_t)spec.tv_nsec;
}

/* Helper for parsing iso formatted datetime string to time_t epoch
 * Format: "1970-01-01T00:00:00+00:00" */
time_t time_parse_iso_date_full_str_to_epoch(const char* _time_str)
{
  struct tm tm = {0};
  int       year, month, day, hour, min, sec, utc_hour, utc_min;
  char      utc_direction;

  if (sscanf(_time_str, ISO_DATE_STR_FULL, &year, &month, &day, &hour,
    &min, &sec, &utc_direction, &utc_hour, &utc_min) != 9)
    return (time_t)-1;

  // +/- UTC
  if (utc_direction == '+')
    tm.tm_hour = hour - utc_hour;
  else if (utc_direction == '-')
    tm.tm_hour = hour + utc_hour;
  else // incorrect string
    return (time_t)-1;

  tm.tm_year  = year - 1900; /* struct tm years since 1900 */
  tm.tm_mon   = month - 1;   /* struct tm months are zero-based */
  tm.tm_mday  = day;
  tm.tm_min   = min;
  tm.tm_sec   = sec;
  tm.tm_isdst = -1; /* Not considering daylight saving time */

  return timegm(&tm);
}

/* Helper for parsing iso8601 formatted datetime string to time_t epoch
 * Format: 1970-01-01T00:00 */
time_t time_parse_iso_date_min_str_to_epoch(const char* _time_str)
{
  struct tm tm = {0};
  int       year, month, day, hour, min;

  if (sscanf(_time_str, ISO_DATE_STR_MIN, &year, &month, &day, &hour, &min) != 5)
    return (time_t)-1;

  tm.tm_year  = year - 1900; /* struct tm years since 1900 */
  tm.tm_mon   = month - 1;   /* struct tm months are zero-based */
  tm.tm_mday  = day;
  tm.tm_hour  = hour;
  tm.tm_min   = min;
  tm.tm_sec   = 0;
  tm.tm_isdst = -1; /* Not considering daylight saving time */

  return timegm(&tm);
}

/* Helper for parsing iso8601 formatted datetime string to time_t epoch
 * Format: "1970-01-01" */
time_t time_parse_iso_date_day_str_to_epoch(const char* _time_str)
{
  struct tm tm = {0};
  int       year, month, day;

  if (sscanf(_time_str, ISO_DATE_STR_DAY, &year, &month, &day) != 3)
    return (time_t)-1;

  tm.tm_year  = year - 1900; /* struct tm years since 1900 */
  tm.tm_mon   = month - 1;   /* struct tm months are zero-based */
  tm.tm_mday  = day;
  tm.tm_hour  = 0;
  tm.tm_min   = 0;
  tm.tm_sec   = 0;
  tm.tm_isdst = -1; /* Not considering daylight saving time */

  return timegm(&tm);
}
