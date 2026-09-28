#include "backtest.h"
#include "test_data.h"

#include <assert.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define EPS_TIGHT 1e-9
#define EPS_LOOSE 1e-3

static int approx_eq(double a, double b, double eps) {
    return fabs(a - b) <= eps;
}

static void test_invalid_inputs(void) {
    printf("Test 1: invalid-input contract... ");

    /* days=3, instruments=2, row-major [day*instruments + i] */
    double base_prices[6] = {100.0, 100.0, 101.0, 99.0, 102.0, 101.0};

    assert(run_backtest(NULL, 2, 3, "BUY_HOLD") == NULL);
    assert(run_backtest(base_prices, 0, 3, "BUY_HOLD") == NULL);
    assert(run_backtest(base_prices, -1, 3, "BUY_HOLD") == NULL);
    assert(run_backtest(base_prices, 2, 1, "BUY_HOLD") == NULL);
    assert(run_backtest(base_prices, 2, 0, "BUY_HOLD") == NULL);
    assert(run_backtest(base_prices, 2, 3, "UNKNOWN_STRATEGY") == NULL);
    assert(run_backtest(base_prices, 2, 3, "") == NULL);

    /* Sanity: the baseline array itself is valid input. */
    BacktestResult* r = run_backtest(base_prices, 2, 3, "BUY_HOLD");
    assert(r != NULL);
    backtest_free_result(r);

    printf("OK\n");
}

static void test_flat_prices(void) {
    printf("Test 2: flat prices (zero movement)... ");

    int instruments = 3, days = 10;
    double prices[30];
    for (int t = 0; t < days; t++) {
        for (int i = 0; i < instruments; i++) {
            prices[t * instruments + i] = 50.0;
        }
    }

    BacktestResult* r = run_backtest(prices, instruments, days, "BUY_HOLD");
    assert(r != NULL);
    assert(approx_eq(r->total_return, 0.0, EPS_TIGHT));
    assert(approx_eq(r->max_drawdown, 0.0, EPS_TIGHT));
    assert(approx_eq(r->sharpe_ratio, 0.0, EPS_TIGHT));
    assert(approx_eq(r->annualized_return, 0.0, EPS_TIGHT));
    backtest_free_result(r);

    printf("OK\n");
}

static void test_hand_computed_uptrend(void) {
    printf("Test 3: hand-computed uptrend (BUY_HOLD, 1 instrument)... ");

    double prices[3] = {100.0, 110.0, 121.0}; /* +10% both days */
    BacktestResult* r = run_backtest(prices, 1, 3, "BUY_HOLD");
    assert(r != NULL);

    assert(approx_eq(r->total_return, 0.21, EPS_TIGHT));
    assert(approx_eq(r->max_drawdown, 0.0, EPS_TIGHT));
    /* Both daily returns are exactly 0.10 -> sample variance is exactly 0. */
    assert(approx_eq(r->sharpe_ratio, 0.0, EPS_TIGHT));

    /* years = 2/252, so annualized = (1+total_return)^(252/2) - 1. Recompute
     * independently rather than assert an exact literal (astronomically
     * large over such a short window). */
    double expected_annualized = pow(1.0 + 0.21, 252.0 / 2.0) - 1.0;
    double rel_err = fabs(r->annualized_return - expected_annualized) / expected_annualized;
    assert(rel_err < EPS_TIGHT);

    backtest_free_result(r);

    printf("OK\n");
}

static void test_hand_computed_drawdown(void) {
    printf("Test 4: hand-computed drawdown (BUY_HOLD, 1 instrument)... ");

    double prices[4] = {100.0, 120.0, 90.0, 108.0}; /* rise, drop, partial recover */
    BacktestResult* r = run_backtest(prices, 1, 4, "BUY_HOLD");
    assert(r != NULL);

    assert(approx_eq(r->total_return, 0.08, EPS_TIGHT));
    /* peak=1.20 at t=1, trough=0.90 at t=2 -> (1.20-0.90)/1.20 = 0.25 */
    assert(approx_eq(r->max_drawdown, 0.25, EPS_TIGHT));
    /* daily returns: +0.20, -0.25, +0.20 -> mean=0.05, std_dev=sqrt(0.0675)
     * ~= 0.259808 -> sharpe = (mean/std_dev)*sqrt(252) ~= 3.05505 */
    assert(approx_eq(r->sharpe_ratio, 3.05505, EPS_LOOSE));

    backtest_free_result(r);

    printf("OK\n");
}

static void test_strategies_diverge(void) {
    printf("Test 5: BUY_HOLD vs REBALANCE_MONTHLY diverge across a rebalance boundary... ");

    int instruments = 2, days = 25; /* t=21 crosses one 21-day rebalance boundary */
    double prices[50];
    for (int t = 0; t < days; t++) {
        prices[t * instruments + 0] = 100.0 + t; /* trending up */
        prices[t * instruments + 1] = 100.0 - t; /* trending down */
    }

    BacktestResult* bh = run_backtest(prices, instruments, days, "BUY_HOLD");
    BacktestResult* rb = run_backtest(prices, instruments, days, "REBALANCE_MONTHLY");
    assert(bh != NULL);
    assert(rb != NULL);

    /* Diverging instruments + a rebalance mid-run must change the outcome. */
    assert(!approx_eq(bh->total_return, rb->total_return, EPS_TIGHT));

    backtest_free_result(bh);
    backtest_free_result(rb);

    printf("OK\n");
}

static void test_realistic_scale_smoke(void) {
    printf("Test 6: realistic-scale smoke test (5y x 500 instruments)... ");

    int instruments = 500;
    int days = 252 * 5;
    size_t n = (size_t)instruments * (size_t)days;

    double* prices = malloc(n * sizeof(double));
    assert(prices != NULL);

    /* base=100, step=0, noise=+-10 -> strictly positive prices in [90, 110],
     * never zero/negative (would break weight/prices[i] in simulate()). */
    test_gen_sample_arr_double(prices, n, 100.0, 0.0, 10.0);

    clock_t start = clock();
    BacktestResult* bh = run_backtest(prices, instruments, days, "BUY_HOLD");
    BacktestResult* rb = run_backtest(prices, instruments, days, "REBALANCE_MONTHLY");
    clock_t end = clock();

    assert(bh != NULL);
    assert(rb != NULL);

    printf("(%.3fs) ", (double)(end - start) / CLOCKS_PER_SEC);

    backtest_free_result(bh);
    backtest_free_result(rb);
    free(prices);

    printf("OK\n");
}

int main(void) {
    test_invalid_inputs();
    test_flat_prices();
    test_hand_computed_uptrend();
    test_hand_computed_drawdown();
    test_strategies_diverge();
    test_realistic_scale_smoke();

    printf("All backtest tests passed.\n");
    return 0;
}
