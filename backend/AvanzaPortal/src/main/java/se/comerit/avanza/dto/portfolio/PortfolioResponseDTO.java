package se.comerit.avanza.dto.portfolio;

import se.comerit.avanza.dto.alerts.AlertsResponseDTO;
import java.util.List;

/**
 * PortfolioResponseDTO is a record that encapsulates the comprehensive view of
 * a user's portfolio,
 * including account summaries, enriched holdings, allocation rows, total
 * portfolio value,
 * recent alerts, any detected drift, and the USD to SEK conversion rate.
 */
public record PortfolioResponseDTO(
                List<AccountSummaryDTO> accountSummary,
                List<EnrichedHoldingDTO> enrichedHoldings,
                List<AllocationRowDTO> allocationRows,
                double totalPortfolioValue,
                List<AlertsResponseDTO> recentAlerts,
                double usdToSek) {
}
