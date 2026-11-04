package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import se.comerit.avanza.config.DriftTresholdConfig;

public class DriftTresholdConfigTest {

    @Test
    void testGetDriftThreshold() {
        DriftTresholdConfig config = new DriftTresholdConfig();
        assertEquals(0.05, config.getDriftThreshold());
    }
}
