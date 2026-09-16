#include "fx_history.hpp"

#include <array>
#include <ctime>
#include <optional>

namespace {

std::optional<std::string> to_date_key(long _unix_time)
{
  std::time_t time = static_cast<std::time_t>(_unix_time);
  std::tm     tm{};
  if (gmtime_r(&time, &tm) == nullptr)
    return std::nullopt;

  std::array<char, 11> buf{}; // "YYYY-MM-DD\0"
  if (std::strftime(buf.data(), buf.size(), "%Y-%m-%d", &tm) == 0)
    return std::nullopt;

  return std::string(buf.data());
}

} // namespace

void FxHistory::set(const std::string& _date, FxTable _table)
{
  if (_date.size() != 10) // "YYYY-MM-DD"
    return;

  by_date_[_date] = std::move(_table);
}

const FxTable* FxHistory::at_or_before(const std::string& _date) const
{
  auto it = by_date_.upper_bound(_date); // first entry strictly after _date
  if (it == by_date_.begin())
    return nullptr; // nothing on or before _date

  --it; // last entry at or before _date
  return &it->second;
}

const FxTable* FxHistory::at_or_before(long _unix_time) const
{
  auto key = to_date_key(_unix_time);
  if (!key)
    return nullptr;

  return at_or_before(*key);
}
