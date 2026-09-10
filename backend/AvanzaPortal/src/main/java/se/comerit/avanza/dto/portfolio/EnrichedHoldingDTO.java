package se.comerit.avanza.dto.portfolio;

/**
 * EnrichedHoldingDTO is a record that represents a detailed view of a holding
 * in a user's portfolio.
 */
public record EnrichedHoldingDTO(
        Long id,
        String ticker,
        String instrumentName,
        double quantity,
        double currentPrice,
        double valueSek,
        double unrealizedReturn,
        double unrealizedReturnPct,
        double sharpe,
        String displayCurrency) {
}
