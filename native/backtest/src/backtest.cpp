#include "backtest.hpp"
#include "volatility.h"
#include "data_utils.h"
#include "port_utils.h"

#include <iostream>
#include <cstring>

BacktestSimulation::BacktestSimulation(
  const double* prices, 
  const int instruments, 
  const int days, 
  const int rebalance_interval_days) 
  : prices_(prices),
    instruments_(instruments),
    days_(days),
    rebalance_interval_(rebalance_interval_days)
{
  nav_series_ = new double[days_];
  returns_    = new double[days_]; // whole portfolio equity curve
  units_      = new double[instruments_];

  // Catch no mem err?
}

BacktestSimulation::~BacktestSimulation() 
{
  reset_calcs();
}

void BacktestSimulation::reset_calcs() 
{
  if (nav_series_)
  {
    delete[] nav_series_;
    nav_series_ = nullptr;
  }
  if (units_)
  {
    delete[] units_;
    units_ = nullptr;
  }
  if (returns_)
  {
    delete[] returns_;
    returns_ = nullptr;
  }
}

int BacktestSimulation::run() 
{
  double weight = 1.0 / instruments_;
  for (int i = 0; i < instruments_; i++) 
    units_[i] = weight / prices_[i];

  /* One NAV per day; risk/'s Sharpe and max-drawdown are both whole-series
   * functions (not running accumulators), so the daily loop just records
   * NAV and hands the finished series to risk/ afterwards. */
  for (int t = 0; t < days_; t++) {
    double nav = 0.0;
    for (int i = 0; i < instruments_; i++) {
      nav += units_[i] * prices_[t * instruments_ + i];
    }
    nav_series_[t] = nav;
    if (rebalance_interval_ > 0 && t > 0 && t % rebalance_interval_ == 0) {
      for (int i = 0; i < instruments_; i++) {
        units_[i] = (weight * nav) / prices_[t * instruments_ + i];
      }
    }
  }

  result_.total_return = nav_series_[days_ - 1] / nav_series_[0] - 1.0;

  double years = (double)(days_ - 1) / TRADING_DAYS_PER_YEAR;
  result_.annualized_return = years > 0.0
    ? pow(1.0 + result_.total_return, 1.0 / years) - 1.0
    : result_.total_return;

  result_.max_drawdown = risk_calc_max_drawdown(nav_series_, (size_t)days_);

  /* returns[0] is data_convert_values_to_returns()'s unused placeholder
   * for the first NAV (no prior day to compare against) - skip it, same
   * as the old loop starting its return calc at t=1. */
  data_convert_values_to_returns(nav_series_, returns_, (size_t)days_);
  result_.sharpe_ratio = risk_calc_sharpe_ratio_double(
    returns_ + 1, (size_t)(days_ - 1), 0.0, TRADING_DAYS_PER_YEAR);


  return 0;
}

/*************** C Interface ****************/

extern "C"  
BacktestResult* run_backtest(
  const double* prices,  
  int instruments,       
  int days,              
  const char* strategy   
)
{
  // Validate inputs
  if (!prices || !strategy || instruments <= 0 || days <= 1) {
    fprintf(stderr, "Invalid inputs");
    return NULL;
  }

  // Check which rebalancing strategy to use
  int rebalance_interval_days;
  if (strcmp(strategy, "BUY_HOLD") == 0) {
    rebalance_interval_days = 0;
  } else if (strcmp(strategy, "REBALANCE_MONTHLY") == 0) {
    rebalance_interval_days = 21;
  } else {
    fprintf(stderr, "Invalid strategy");
    return NULL;
  }

  // Init engine, simulate, get results
  BacktestSimulation BtS(prices, instruments, days, rebalance_interval_days);
  int res = BtS.run();
  if (res != 0)
  {
    fprintf(stderr, "BacktestSimulation::simulate");
    return NULL;
  }

  // Allocate Results struct and copy results
  BacktestResult* BtR = (BacktestResult*)calloc(0, sizeof(BacktestResult));
  if (!BtR) {
    fprintf(stderr, "calloc");
    return NULL;
  }
  memcpy(BtR, BtS.get_result(), sizeof(BacktestResult));

  return BtR;
}

extern "C"  
void backtest_free_result(BacktestResult* result)
{
  if (result) 
  {
    free(result);
    result = NULL;
  }
}

