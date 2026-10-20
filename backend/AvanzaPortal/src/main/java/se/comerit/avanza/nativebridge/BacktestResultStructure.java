package se.comerit.avanza.nativebridge;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This class tells JNA how to read the result the result returned by
 * run_backtest
 */
@Structure.FieldOrder({ "total_return", "annualized_return", "max_drawdown", "sharpe_ratio" })
public class BacktestResultStructure extends Structure {

    public double total_return;
    public double annualized_return;
    public double max_drawdown;
    public double sharpe_ratio;

    public BacktestResultStructure() {
    }

    public BacktestResultStructure(Pointer pointer) {
        super(pointer);
        read();
    }
}