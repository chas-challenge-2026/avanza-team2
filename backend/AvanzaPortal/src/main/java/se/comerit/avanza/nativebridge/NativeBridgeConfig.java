package se.comerit.avanza.nativebridge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NativeBridgeConfig {

    @Bean
    public FxLibrary fxLibrary() {
        return FxLibraryLoader.INSTANCE;
    }
}