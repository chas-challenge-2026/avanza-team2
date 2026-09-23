package se.comerit.avanza.service;

import org.springframework.context.annotation.Configuration;

/**
 * Provides singel source of truth for threshold values instead of hardcoding
 * them in each class like V1.
 * 
 * When the allocation drifts outside of the acceptable range, rebalancing
 * occurs to bring the portfolio back to target
 * 
 * Using the simple 60% stock and 40% bond example, a 5% threshold could be
 * established.
 * TresholdConfig
 */
@Configuration
public class DriftTresholdConfig {
    private DriftTresholdConfig() {
    }

    public static final double ALLOCATION_DRIFT_THRESHOLD = 0.05;

    public double getDriftThreshold() {
        return ALLOCATION_DRIFT_THRESHOLD;
    }
}