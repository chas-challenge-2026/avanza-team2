#pragma once

#include "fx_table.hpp"

#include <map>
#include <string>

/*
Historical ECB reference rates, one FxTable per trading day.

ECB only publishes on business days, so a lookup for an arbitrary date
resolves to the most recent trading day at or before it.
*/

class FxHistory
{
public:

  /**
   * @brief Records a trading day's rate table.
   * @param _date Date key, "YYYY-MM-DD"; ignored when not that shape.
   * @param _table Rate table for that day.
   */

  void set(const std::string& _date, FxTable _table);

  /**
   * @brief Looks up the most recent trading day at or before _date.
   * @param _date Date, "YYYY-MM-DD".
   * @return The day's rate table, or nullptr if _date is before the earliest entry.
   */

  const FxTable* at_or_before(const std::string& _date) const;

  /**
   * @brief Same lookup as at_or_before(const std::string&), from a Unix
   * timestamp.
   * @param _unix_time Timestamp, interpreted as UTC.
   * @return The day's rate table, or nullptr if none applies.
   */

  const FxTable* at_or_before(long _unix_time) const;

  std::size_t size() const { return by_date_.size(); }
  bool        empty() const { return by_date_.empty(); }

private:
  std::map<std::string, FxTable> by_date_;
};
