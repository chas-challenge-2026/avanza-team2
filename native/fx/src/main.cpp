#include "ecb_source.hpp"
#include "fx_table.hpp"

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

} // namespace

int main(int _argc, char** _argv)
{
  std::cout << std::fixed << std::setprecision(4);

  test_parse();

  if (_argc > 1 && std::string_view(_argv[1]) == "--live") {
    std::cout << "\n--- live ECB fetch ---\n";
    test_live();
  }

  return 0;
}
