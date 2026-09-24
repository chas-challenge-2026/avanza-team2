#pragma once

#include "history.hpp"
#include "table.hpp"

#include <optional>
#include <string_view>

/*
ECB euro foreign exchange reference rates (eurofxref).

  latest day : https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
  full hist  : https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.xml

Every rate sits in an element like

  <Cube currency="USD" rate="1.1032"/>

under a <Cube time="YYYY-MM-DD"> element for the trading day. The daily file
has exactly one such block; the historical file repeats it, one per trading
day, all nested inside a wrapping <Cube> element.
*/

namespace ecb {

inline constexpr std::string_view daily_url =
  "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
inline constexpr std::string_view hist_url =
  "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.xml";

/**
 * @brief Parses a single-day eurofxref XML document, the daily file or one <Cube time="..."> block from a historical file.
 * @param _xml Document body.
 * @return A table with at least the seeded EUR entry; unparseable entries are skipped.
 */
FxTable parse_xml(std::string_view _xml);

/**
 * @brief Fetches the latest daily reference rates over HTTP.
 * @return The rate table, or nullopt on a network error or an empty document.
 */
std::optional<FxTable> fetch_latest();

/**
 * @brief Parses a eurofxref-hist.xml document into one FxTable per
 * trading day.
 * @param _xml Document body.
 * @return A history keyed by each trading day's "YYYY-MM-DD" date.
 */
FxHistory parse_hist_xml(std::string_view _xml);

/**
 * @brief Fetches the full ECB rate history back to 1999, over HTTP.
 * @return The history, or nullopt on a network error or an empty document.
 */
std::optional<FxHistory> fetch_history();

} // namespace ecb
