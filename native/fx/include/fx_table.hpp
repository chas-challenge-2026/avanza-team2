#pragma once

#include <optional>
#include <string>
#include <string_view>
#include <unordered_map>

/*
Currency conversion table.

Rates are stored the way the ECB publishes them: "units of currency per EUR".
EUR is always present with a rate of 1.0, so any pair resolves as a cross rate
through EUR without special casing the base currency.
*/
class FxTable
{
public:
  FxTable();

  // Ignored when the code is empty or the rate is not positive.
  void set(std::string_view _currency, double _per_eur);

  // rate(from -> to) = (to per EUR) / (from per EUR)
  std::optional<double> rate(std::string_view _from, std::string_view _to) const;

  std::optional<double> per_eur(std::string_view _currency) const;

  const std::unordered_map<std::string, double>& rates() const { return rates_; }

  std::size_t size() const { return rates_.size(); }
  bool        empty() const { return rates_.empty(); }

private:
  std::unordered_map<std::string, double> rates_;
};
