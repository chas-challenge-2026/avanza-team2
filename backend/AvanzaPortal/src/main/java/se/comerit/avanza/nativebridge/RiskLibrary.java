package se.comerit.avanza.nativebridge;

import com.sun.jna.Library;

public interface RiskLibrary extends Library {
    double risk_calc_sharpe_ratio_double(double[] data, long length, double riskFreeRate, long yearFreq);

    double risk_calc_volatility_double(double[] data, int length);

    float risk_calc_volatility_float(float[] data, int length);

    double risk_calc_volatility_int32_t(int[] data, int length);
}
