#include "ecb_source.hpp"
#include "history.hpp"
#include "table.hpp"

#include <iomanip>
#include <iostream>
#include <string_view>

namespace {

// A trimmed eurofxref-daily.xml payload so the module self-tests in CI
// without reaching for the network.
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

// A trimmed eurofxref-hist.xml payload: two trading days, with a weekend
// gap between them and the lookup date below.
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

void print_rate(const FxTable& _table, std::string_view _from, std::string_view _to)
{
  std::cout << "  1 " << _from << " = ";
  if (auto rate = _table.rate(_from, _to))
    std::cout << *rate << ' ' << _to << '\n';
  else
    std::cout << "(unknown)\n";
}

void test_parse()
{
  FxTable table = ecb::parse_xml(sample_xml);

  std::cout << "Parsed " << table.size() << " quotes from sample:\n";
  for (const auto& [currency, per_eur] : table.rates())
    std::cout << "  1 EUR = " << per_eur << ' ' << currency << '\n';

  std::cout << '\n';
  print_rate(table, "SEK", "USD");
  print_rate(table, "USD", "SEK");
}

void test_parse_history()
{
  FxHistory history = ecb::parse_hist_xml(sample_hist_xml);

  std::cout << "Parsed " << history.size() << " trading days from history sample:\n";

  if (const FxTable* table = history.at_or_before("2026-09-04")) {
    std::cout << "  2026-09-04: ";
    print_rate(*table, "USD", "SEK");
  }

  // Weekend gap: 09-05/06 have no data, so this should fall back to 09-04.
  if (const FxTable* table = history.at_or_before("2026-09-06")) {
    std::cout << "  2026-09-06 (weekend, falls back): ";
    print_rate(*table, "USD", "SEK");
  }

  if (history.at_or_before("2020-01-01") != nullptr)
    std::cout << "  unexpected: resolved a date before the sample range\n";
}

void test_live()
{
  auto table = ecb::fetch_latest();
  if (!table) {
    std::cout << "live fetch failed\n";
    return;
  }

  print_rate(*table, "EUR", "USD");
  print_rate(*table, "EUR", "SEK");
  print_rate(*table, "USD", "SEK");
}

void test_live_history()
{
  auto history = ecb::fetch_history();
  if (!history) {
    std::cout << "live history fetch failed\n";
    return;
  }

  std::cout << "Fetched " << history->size() << " trading days.\n";

  if (const FxTable* table = history->at_or_before("2021-01-04")) {
    std::cout << "  ~5y ago: ";
    print_rate(*table, "USD", "SEK");
  }
}

} // namespace

int main(int _argc, char** _argv)
{
  std::cout << std::fixed << std::setprecision(4);

  test_parse();
  std::cout << '\n';
  test_parse_history();

  if (_argc > 1 && std::string_view(_argv[1]) == "--live") {
    std::cout << "\nlive ECB fetch\n";
    test_live();

    std::cout << "\nlive ECB history fetch\n";
    test_live_history();
  }

  return 0;
}
