package se.comerit.avanza.dto.portfolio;

import java.time.LocalDate;

/**
 * Represents a historical portfolio performance data point.
 */
public record HistoricalPerformancePointDTO(
        LocalDate date,
        double portfolioPct,
        Double indexPct
) {
}