#include "backtest.h"
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

    double prev_nav = 0.0, first_nav = 0.0, peak = 0.0, max_dd = 0.0;
    double sum_r = 0.0, sum_r2 = 0.0;

    for (int t = 0; t < days; t++) {
        double nav = 0.0;
        for (int i = 0; i < instruments; i++) {
            nav += units[i] * prices[t * instruments + i];
        }

        if (t == 0) {
            first_nav = nav;
            peak = nav;
        } else {
            double r = nav / prev_nav - 1.0;
            sum_r += r;
            sum_r2 += r * r;
        }

        if (nav > peak) peak = nav;
        double dd = (peak - nav) / peak;
        if (dd > max_dd) max_dd = dd;

        prev_nav = nav;

        if (rebalance_interval_days > 0 && t > 0 && t % rebalance_interval_days == 0) {
            for (int i = 0; i < instruments; i++) {
                units[i] = (weight * nav) / prices[t * instruments + i];
            }
        }
    }

    free(units);

    BacktestResult result;
    result.total_return = prev_nav / first_nav - 1.0;

    double years = (double)(days - 1) / TRADING_DAYS_PER_YEAR;
    result.annualized_return = years > 0.0
        ? pow(1.0 + result.total_return, 1.0 / years) - 1.0
        : result.total_return;

    result.max_drawdown = max_dd;

    int n = days - 1;
    double mean = sum_r / n;
    double variance = n > 1 ? (sum_r2 - n * mean * mean) / (n - 1) : 0.0;
    double std_dev = sqrt(variance);

    result.sharpe_ratio = std_dev > 1e-12
        ? (mean / std_dev) * sqrt((double)TRADING_DAYS_PER_YEAR)
        : 0.0;

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
