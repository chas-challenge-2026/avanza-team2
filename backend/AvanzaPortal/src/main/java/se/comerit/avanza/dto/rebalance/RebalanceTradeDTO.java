package se.comerit.avanza.dto.rebalance;

import java.math.BigDecimal;

/**
 * One proposed buy/sell of a holding. Value is expressed in SEK.
 */
public record RebalanceTradeDTO(
        Long holdingId,
        String ticker,
        String category,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal value) {
}
