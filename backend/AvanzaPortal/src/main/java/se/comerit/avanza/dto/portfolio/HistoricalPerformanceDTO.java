package se.comerit.avanza.dto.portfolio;

import java.util.List;

/**
 * Contains the historical performance of the current portfolio.
 */
public record HistoricalPerformanceDTO(
        double startValue,
        List<HistoricalPerformancePointDTO> points,
        List<String> unavailableTickers,
        String warning
) {
}