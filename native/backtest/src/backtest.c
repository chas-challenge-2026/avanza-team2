#include "backtest.h"
#include "volatility.h"
#include "data_utils.h"
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define TRADING_DAYS_PER_YEAR 252

/* rebalance_interval_days: 0 = never rebalance (BUY_HOLD), >0 = rebalance
 * back to equal weight every N days (REBALANCE_MONTHLY uses 21). */
static BacktestResult simulate(const double* prices, int instruments, int days, int rebalance_interval_days) {
    double weight = 1.0 / instruments;
    double* units = malloc(sizeof(double) * instruments);
    for (int i = 0; i < instruments; i++) {
        units[i] = weight / prices[i];
    }

    /* One NAV per day; risk/'s Sharpe and max-drawdown are both whole-series
     * functions (not running accumulators), so the daily loop just records
     * NAV and hands the finished series to risk/ afterwards. */
    double* nav_series = malloc(sizeof(double) * days);

    for (int t = 0; t < days; t++) {
        double nav = 0.0;
        for (int i = 0; i < instruments; i++) {
            nav += units[i] * prices[t * instruments + i];
        }
        nav_series[t] = nav;

        if (rebalance_interval_days > 0 && t > 0 && t % rebalance_interval_days == 0) {
            for (int i = 0; i < instruments; i++) {
                units[i] = (weight * nav) / prices[t * instruments + i];
            }
        }
    }

    free(units);

    BacktestResult result;
    result.total_return = nav_series[days - 1] / nav_series[0] - 1.0;

    double years = (double)(days - 1) / TRADING_DAYS_PER_YEAR;
    result.annualized_return = years > 0.0
        ? pow(1.0 + result.total_return, 1.0 / years) - 1.0
        : result.total_return;

    result.max_drawdown = risk_calc_max_drawdown(nav_series, (size_t)days);

    /* returns[0] is data_convert_values_to_returns()'s unused placeholder
     * for the first NAV (no prior day to compare against) - skip it, same
     * as the old loop starting its return calc at t=1. */
    double* returns = malloc(sizeof(double) * days);
    data_convert_values_to_returns(nav_series, returns, (size_t)days);
    result.sharpe_ratio = risk_calc_sharpe_ratio_double(
        returns + 1, (size_t)(days - 1), 0.0, TRADING_DAYS_PER_YEAR);

    free(returns);
    free(nav_series);

    return result;
}

BacktestResult* run_backtest(const double* prices, int instruments, int days, const char* strategy) {
    if (!prices || !strategy || instruments <= 0 || days <= 1) {
        return NULL;
    }

    int rebalance_interval_days;
    if (strcmp(strategy, "BUY_HOLD") == 0) {
        rebalance_interval_days = 0;
    } else if (strcmp(strategy, "REBALANCE_MONTHLY") == 0) {
        rebalance_interval_days = 21;
    } else {
        return NULL;
    }

    BacktestResult computed = simulate(prices, instruments, days, rebalance_interval_days);

    BacktestResult* out = malloc(sizeof(BacktestResult));
    *out = computed;
    return out;
}

void backtest_free_result(BacktestResult* result) {
    free(result);
}
