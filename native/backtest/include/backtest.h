#pragma once

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    double total_return;
    double annualized_return;
    double max_drawdown;
    double sharpe_ratio;
} BacktestResult;

/* prices: row-major, len = days*instruments, prices[day*instruments + i].
 * strategy: "BUY_HOLD" | "REBALANCE_MONTHLY".
 *
 * Returns a heap-allocated BacktestResult*, or NULL on invalid input
 * (unknown strategy, instruments<=0, days<=1, prices==NULL).
 * Caller must release a non-NULL return value via backtest_free_result().
 */
BacktestResult* run_backtest(const double* prices, int instruments, int days, const char* strategy);

void backtest_free_result(BacktestResult* result);

#ifdef __cplusplus
}
#endif
