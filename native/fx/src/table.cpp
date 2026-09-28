#include "table.hpp"

FxTable::FxTable()
{
  rates_.emplace("EUR", 1.0);
}

void FxTable::set(std::string_view _currency, double _per_eur)
{
  if (_currency.empty() || _per_eur <= 0.0)
    return;

  rates_[std::string(_currency)] = _per_eur;
}

std::optional<double> FxTable::per_eur(std::string_view _currency) const
{
  auto it = rates_.find(std::string(_currency));
  if (it == rates_.end())
    return std::nullopt;

  return it->second;
}

std::optional<double> FxTable::rate(std::string_view _from, std::string_view _to) const
{
  auto from = per_eur(_from);
  auto to   = per_eur(_to);
  if (!from || !to)
    return std::nullopt;

  return *to / *from;
}
