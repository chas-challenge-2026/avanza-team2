package se.comerit.avanza.nativebridge;

import com.sun.jna.Native;

public class FxLibraryLoader {
    public static final FxLibrary INSTANCE = Native.load("fx", FxLibrary.class);

    private FxLibraryLoader() {
    }
}
