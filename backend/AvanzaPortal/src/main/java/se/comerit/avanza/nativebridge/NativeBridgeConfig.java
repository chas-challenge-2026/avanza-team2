package se.comerit.avanza.nativebridge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides beans for accessing the native FX and Risk libraries.
 * NativeBridgeConfig
 */
@Configuration
public class NativeBridgeConfig {

    @Bean
    public FxLibrary fxLibrary() {
        return FxLibraryLoader.INSTANCE;
    }

    @Bean
    public RiskLibrary riskLibrary() {
        return RiskLibraryLoader.INSTANCE;
    }
}
