package se.comerit.avanza.dto.portfolioDto;

import se.comerit.avanza.entity.Alerts;
import java.util.List;
import java.util.Map;

/**
 * PortfolioResponseDTO is a record that encapsulates the comprehensive view of
 * a user's portfolio,
 * including account summaries, enriched holdings, allocation rows, total
 * portfolio value,
 * recent alerts, any detected drift, and the USD to SEK conversion rate.
 */
public record PortfolioResponseDTO(
                List<Map<String, Object>> accountSummary,
                List<Map<String, Object>> enrichedHoldings,
                List<Map<String, Object>> allocationRows,
                double totalPortfolioValue,
                List<Alerts> recentAlerts,
                boolean anyDrift,
                double usdToSek) {
}
