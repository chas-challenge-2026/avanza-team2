package se.comerit.avanza.nativebridge;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Interface for interacting with the native backtest library.
 * BacktestLibrary
 */
public interface BacktestLibrary extends Library {

    /**
     * Runs a backtest for the specified strategy using the provided price data.
     * 
     * @param prices      An array of historical prices.
     * @param instruments The number of instruments in the backtest.
     * @param days        The number of days to include in the backtest.
     * @param strategy    The name of the strategy to backtest.
     * @return A pointer to the result of the backtest, which should be freed using
     *         {@link #backtest_free_result(Pointer)} when no longer needed.
     */
    Pointer run_backtest(
            double[] prices,
            int instruments,
            int days,
            String strategy);

    /**
     * Frees the memory allocated for the result of a backtest.
     * 
     * @param result A pointer to the result of the backtest to be freed.
     */
    void backtest_free_result(Pointer result);
}
