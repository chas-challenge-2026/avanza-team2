package se.comerit.avanza.nativebridge;

import com.sun.jna.Library;

/**
 * Native bridge interface for foreign exchange (FX) operations.
 * Provides methods to fetch FX rates and perform currency conversions.
 */
public interface FxLibrary extends Library {
    double fx_rate(String from, String to, long date);

    double fx_convert(double amount, String from, String to, long date);
}
