package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DriftTresholdConfigTest {

    @Test
    void testGetDriftThreshold() {
        DriftTresholdConfig config = new DriftTresholdConfig();
        assertEquals(0.05, config.getDriftThreshold());
    }
}
