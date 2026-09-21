package se.comerit.avanza.nativebridge;

import com.sun.jna.Native;

public class RiskLibraryLoader {

    public static final RiskLibrary INSTANCE = (RiskLibrary) Native.load("risk", RiskLibrary.class);

    private RiskLibraryLoader() {
    }
}
