package se.comerit.avanza.nativebridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Diagnostik för #33. Bevisar att libpoc.so laddas och att JNA-roundtripen
// funkar i den körande containern, inte bara i det lokala dlopen testet.
@RestController
public class NativeDiagController {

    private static final Logger log = LoggerFactory.getLogger(NativeDiagController.class);

    @EventListener(ApplicationReadyEvent.class)
    public void logRoundTripAtStartup() {
        int result = PocLibrary.INSTANCE.poc_add_one(41);
        log.info("native PoC: poc_add_one(41) = {}", result);
    }

    @GetMapping("/diag/native")
    public Map<String, Object> nativeRoundTrip() {
        int input = 41;
        int result = PocLibrary.INSTANCE.poc_add_one(input);
        return Map.of(
            "input", input,
            "poc_add_one", result,
            "ok", result == input + 1
        );
    }
}
