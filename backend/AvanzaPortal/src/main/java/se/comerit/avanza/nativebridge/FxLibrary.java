package se.comerit.avanza.nativebridge;

import com.sun.jna.Library;

public interface FxLibrary extends Library {
    double fx_rate(String from, String to, long date);

    double fx_convert(double amount, String from, String to, long date);
}
