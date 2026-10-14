package se.comerit.avanza.nativebridge;

import com.sun.jna.Native;

/**
 * Loads the native backtest library using JNA.
 * BacktestLibraryLoader
 */
public class BacktestLibraryLoader {
    public static final BacktestLibrary INSTANCE = Native.load("backtest", BacktestLibrary.class);

    private BacktestLibraryLoader() {
        // Prevent instantiation
    }
}
