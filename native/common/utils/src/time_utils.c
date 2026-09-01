#define _POSIX_C_SOURCE 199309L
#include "time_utils.h"

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

