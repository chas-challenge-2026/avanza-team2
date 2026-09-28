package se.comerit.avanza.nativebridge;

import com.sun.jna.Native;

/**
 * Loader class for the FX native library.
 * Provides a singleton instance of the FxLibrary interface.
 */
public class FxLibraryLoader {
    public static final FxLibrary INSTANCE = Native.load("fx", FxLibrary.class);

    private FxLibraryLoader() {
    }
}
