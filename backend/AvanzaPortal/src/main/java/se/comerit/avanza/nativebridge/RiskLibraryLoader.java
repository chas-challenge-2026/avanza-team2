package se.comerit.avanza.nativebridge;

import com.sun.jna.Native;

/**
 * Loader class for the native risk library.
 * Provides a singleton instance of the RiskLibrary interface.
 *
 * If the native library is missing or the symbol signature does not match the
 * RiskLibraryLoader
 */
public class RiskLibraryLoader {
    public static final RiskLibrary INSTANCE = (RiskLibrary) Native.load("risk", RiskLibrary.class);

    private RiskLibraryLoader() {
    }
}
