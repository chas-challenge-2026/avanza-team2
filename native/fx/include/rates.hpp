#pragma once

#include <chrono>

/*
Decides when the cached ECB tables in rates.cpp are out of date.
*/

namespace rates {

using Clock = std::chrono::system_clock;

/**
 * @brief Start of the cache day. ECB publishes around 16:00 CET, which is
 * 15:00 UTC in winter and 14:00 UTC in summer.
 */
inline constexpr auto publish_offset = std::chrono::hours(15) + std::chrono::minutes(30);

/**
 * @brief Checks whether two times fall on the same UTC calendar day.
 * @param _a First time.
 * @param _b Second time.
 * @return True if both are on the same UTC day.
 */
bool same_utc_day(Clock::time_point _a, Clock::time_point _b);

/**
 * @brief Checks whether two times fall between the same two ECB publications.
 * @param _a Time the cached table was fetched.
 * @param _b Time of the current lookup.
 * @return True if the table fetched at _a is still current at _b.
 */
bool same_publication_day(Clock::time_point _a, Clock::time_point _b);

} // namespace rates
