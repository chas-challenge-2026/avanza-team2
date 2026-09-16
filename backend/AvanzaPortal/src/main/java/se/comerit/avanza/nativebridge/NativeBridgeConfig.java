package se.comerit.avanza.nativebridge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for the native bridge.
 * Provides beans for accessing the FX native library.
 */
@Configuration
public class NativeBridgeConfig {

    @Bean
    public FxLibrary fxLibrary() {
        return FxLibraryLoader.INSTANCE;
    }
}