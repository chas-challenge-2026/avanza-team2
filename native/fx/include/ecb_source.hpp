#pragma once

#include "fx_table.hpp"

#include <optional>
#include <string_view>

/*
ECB euro foreign exchange reference rates (eurofxref).

  latest day : https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
  last 90d   : https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml
  full hist  : https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.xml

Every rate sits in an element like

  <Cube currency="USD" rate="1.1032"/>

under a <Cube time="YYYY-MM-DD"> element for the trading day.
*/
namespace ecb {

inline constexpr std::string_view daily_url =
  "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
inline constexpr std::string_view hist_90d_url =
  "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";

// Parses a eurofxref XML document. Unparseable entries are skipped; the
// returned table always has at least the seeded EUR entry.
FxTable parse_xml(std::string_view _xml);

// Fetches the latest daily reference rates over HTTP. Returns nullopt on a
// network error or when the document yields no rates.
std::optional<FxTable> fetch_latest();

} // namespace ecb
