package se.comerit.avanza.nativebridge;

import com.sun.jna.Library;
import com.sun.jna.Native;

// PoC for #30. Loads libpoc.so and exposes poc_add_one for a real round-trip test.
public interface PocLibrary extends Library {

    PocLibrary INSTANCE = Native.load("poc", PocLibrary.class);

    int poc_add_one(int value);
}
