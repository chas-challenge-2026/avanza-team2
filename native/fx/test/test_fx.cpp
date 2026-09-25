#include "ecb.hpp"
#include "history.hpp"
#include "rates.h"
#include "rates.hpp"
#include "table.hpp"

#include <cmath>
#include <ctime>
#include <iostream>
#include <optional>
#include <string_view>

namespace {

int checks_run    = 0;
int checks_failed = 0;

bool approx_equal(double _a, double _b, double _eps = 1e-9)
{
  return std::abs(_a - _b) < _eps;
}

} // namespace

// Counts every check and prints the failing expression with its line number.
// Unlike assert() it keeps running after a failure and is not removed by NDEBUG.
#define CHECK(_cond)                                                                  \
  do {                                                                                \
    ++checks_run;                                                                     \
    if (!(_cond)) {                                                                   \
      ++checks_failed;                                                                \
      std::cout << "  [FAIL] " << __FILE__ << ':' << __LINE__ << ": " << #_cond << '\n'; \
    }                                                                                 \
  } while (false)

namespace {

// A trimmed eurofxref-daily.xml payload, so parsing is tested without the network.
constexpr std::string_view sample_xml = R"(
<gesmes:Envelope>
  <Cube>
    <Cube time='2026-09-04'>
      <Cube currency='USD' rate='1.1032'/>
      <Cube currency='SEK' rate='11.1875'/>
      <Cube currency='GBP' rate='0.8621'/>
    </Cube>
  </Cube>
</gesmes:Envelope>)";

// A trimmed eurofxref-hist.xml payload with two trading days. 2026-09-05 and
// 2026-09-06 are a weekend, so ECB has no rates for them.
constexpr std::string_view sample_hist_xml = R"(
<gesmes:Envelope>
  <Cube>
    <Cube time='2026-09-04'>
      <Cube currency='USD' rate='1.1032'/>
      <Cube currency='SEK' rate='11.1875'/>
    </Cube>
    <Cube time='2026-09-03'>
      <Cube currency='USD' rate='1.1020'/>
      <Cube currency='SEK' rate='11.1500'/>
    </Cube>
  </Cube>
</gesmes:Envelope>)";

void test_table()
{
  std::cout << "FxTable\n";

  FxTable table;
  table.set("USD", 1.1032);
  table.set("SEK", 11.1875);

  CHECK(approx_equal(table.per_eur("EUR").value_or(0.0), 1.0));
  CHECK(approx_equal(table.per_eur("USD").value_or(0.0), 1.1032));
  CHECK(!table.per_eur("XXX"));

  CHECK(approx_equal(table.rate("USD", "SEK").value_or(0.0), 11.1875 / 1.1032));
}

void test_parse()
{
  std::cout << "parse_xml\n";

  FxTable table = ecb::parse_xml(sample_xml);
  CHECK(table.size() == 4); // EUR is always added, plus USD, SEK and GBP
  CHECK(table.date() == "2026-09-04");

  CHECK(approx_equal(table.per_eur("USD").value_or(0.0), 1.1032));
  CHECK(approx_equal(table.per_eur("SEK").value_or(0.0), 11.1875));
  CHECK(approx_equal(table.per_eur("GBP").value_or(0.0), 0.8621));
}

void test_parse_history()
{
  std::cout << "parse_hist_xml\n";

  FxHistory history = ecb::parse_hist_xml(sample_hist_xml);
  CHECK(history.size() == 2);

  const FxTable* sep_3 = history.at_or_before("2026-09-03");
  CHECK(sep_3 != nullptr);
  if (sep_3) {
    CHECK(approx_equal(sep_3->per_eur("USD").value_or(0.0), 1.1020));
    CHECK(sep_3->date() == "2026-09-03");
  }

  const FxTable* sep_4 = history.at_or_before("2026-09-04");
  CHECK(sep_4 != nullptr);
  if (sep_4)
    CHECK(approx_equal(sep_4->per_eur("USD").value_or(0.0), 1.1032));

  // A weekend date uses the Friday before it.
  CHECK(history.at_or_before("2026-09-06") == sep_4);

  // A date before the first entry has no rates.
  CHECK(history.at_or_before("2020-01-01") == nullptr);
}

// Builds a UTC time point from a calendar date and time of day.
rates::Clock::time_point utc(int _year, int _month, int _day, int _hour, int _minute)
{
  std::tm tm{};
  tm.tm_year = _year - 1900;
  tm.tm_mon  = _month - 1;
  tm.tm_mday = _day;
  tm.tm_hour = _hour;
  tm.tm_min  = _minute;
  return rates::Clock::from_time_t(timegm(&tm));
}

void test_rollover()
{
  std::cout << "cache rollover\n";

  using rates::same_publication_day;
  using rates::same_utc_day;

  // Plain UTC days still split at midnight.
  CHECK(same_utc_day(utc(2026, 9, 24, 0, 1), utc(2026, 9, 24, 23, 59)));
  CHECK(!same_utc_day(utc(2026, 9, 24, 23, 59), utc(2026, 9, 25, 0, 1)));

  // The publication day rolls over at 15:30 UTC.
  CHECK(same_publication_day(utc(2026, 9, 24, 10, 0), utc(2026, 9, 24, 15, 29)));
  CHECK(!same_publication_day(utc(2026, 9, 24, 15, 29), utc(2026, 9, 24, 15, 31)));

  // Midnight is no longer a boundary, so an evening fetch is still current
  // the next morning until that day's publication.
  CHECK(same_publication_day(utc(2026, 9, 24, 15, 31), utc(2026, 9, 25, 15, 29)));
  CHECK(!same_publication_day(utc(2026, 9, 24, 15, 31), utc(2026, 9, 25, 15, 31)));

  // Year end, where tm_yday wraps back to 0.
  CHECK(same_publication_day(utc(2026, 12, 31, 16, 0), utc(2027, 1, 1, 10, 0)));
  CHECK(!same_publication_day(utc(2026, 12, 31, 16, 0), utc(2027, 1, 1, 16, 0)));
}

void test_refresh()
{
  std::cout << "cache refresh\n";

  // A fake ECB that counts fetches and can be taken down.
  rates::Cached<int> cache;
  int  fetches = 0;
  int  next    = 1;
  bool ecb_up  = false;

  auto fetch = [&]() -> std::optional<int> {
    ++fetches;
    return ecb_up ? std::optional(next) : std::nullopt;
  };
  auto get = [&](rates::Clock::time_point _now) {
    auto value = rates::refresh(cache, fetch, _now);
    return value ? *value : 0;
  };

  // ECB down with nothing cached, then no new try until 5 minutes have passed.
  CHECK(get(utc(2026, 9, 24, 10, 0)) == 0 && fetches == 1);
  CHECK(get(utc(2026, 9, 24, 10, 2)) == 0 && fetches == 1);

  ecb_up = true;
  CHECK(get(utc(2026, 9, 24, 10, 5)) == 1 && fetches == 2);
  CHECK(get(utc(2026, 9, 24, 14, 0)) == 1 && fetches == 2);

  // A failed fetch after the 15:30 rollover keeps the old value and tries
  // again after 5 minutes instead of waiting for the next day.
  ecb_up = false;
  next   = 2;
  CHECK(get(utc(2026, 9, 24, 15, 31)) == 1 && fetches == 3);
  CHECK(get(utc(2026, 9, 24, 15, 34)) == 1 && fetches == 3);

  ecb_up = true;
  CHECK(get(utc(2026, 9, 24, 15, 36)) == 2 && fetches == 4);
}

// Only run with --live, since these reach ECB over the network.
void test_live()
{
  std::cout << "fetch_latest (--live)\n";

  auto table = ecb::fetch_latest();
  CHECK(table.has_value());
  if (!table)
    return;

  CHECK(table->per_eur("USD").value_or(-1.0) > 0.0);
  CHECK(table->rate("USD", "SEK").value_or(-1.0) > 0.0);

  // ECB never skips more than a long weekend, so the latest rate is recent.
  long now  = static_cast<long>(std::time(nullptr));
  long date = fx_rate_date(0);
  CHECK(date > now - 6 * 24 * 3600 && date <= now);
}

void test_live_history()
{
  std::cout << "fetch_history (--live)\n";

  auto history = ecb::fetch_history();
  CHECK(history.has_value());
  if (!history)
    return;

  CHECK(history->size() > 1000); // ECB history goes back to 1999

  const FxTable* jan_2021 = history->at_or_before("2021-01-04");
  CHECK(jan_2021 != nullptr);
  if (jan_2021)
    CHECK(jan_2021->rate("USD", "SEK").value_or(-1.0) > 0.0);
}

} // namespace

int main(int _argc, char** _argv)
{
  test_table();
  test_parse();
  test_parse_history();
  test_rollover();
  test_refresh();

  if (_argc > 1 && std::string_view(_argv[1]) == "--live") {
    test_live();
    test_live_history();
  }

  std::cout << '\n' << (checks_run - checks_failed) << '/' << checks_run << " checks passed\n";

  return checks_failed == 0 ? 0 : 1;
}
