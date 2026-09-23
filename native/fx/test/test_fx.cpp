#include "table.hpp"

#include <cmath>
#include <iostream>

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

} // namespace

int main()
{
  test_table();

  std::cout << '\n' << (checks_run - checks_failed) << '/' << checks_run << " checks passed\n";

  return checks_failed == 0 ? 0 : 1;
}
