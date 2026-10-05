#pragma once

#include "backtest.h"
#include "port_utils.h"
#include <cstdint>

static size_t TRADING_DAYS_PER_YEAR = 252;

class BacktestSimulation
{
public:

  BacktestSimulation(
    const double* prices,             // Price-timeseries, len=days*instruments
    const int instruments,            // Amount of instruments invested in
    const int days,                   // Amount of days invested
    const int rebalance_interval_days // "BUY_HOLD", "REBALANCE_MONTHLY", etc.
  );
  ~BacktestSimulation();

  // No copy, no move
  BacktestSimulation(const BacktestSimulation&) = delete;
  BacktestSimulation& operator=(const BacktestSimulation&) = delete;
  BacktestSimulation(BacktestSimulation&&) noexcept = delete;
  BacktestSimulation& operator=(BacktestSimulation&&) noexcept = delete;

  // Runs simulation backtest and fills result_
  int run();

  // Frees memory for calculated data, allowing multiple runs 
  void reset_calcs();

  const BacktestResult* get_result() const { return &result_; }

private:
  BacktestResult result_;

  // User inputted values
  const double* prices_             = nullptr;
  const int     instruments_        = 0;
  const int     days_               = 0;
  const int     rebalance_interval_ = 0;

  // Backtest calculated arrays
  double* nav_series_ = nullptr;
  double* units_      = nullptr;
  double* returns_    = nullptr;

};

