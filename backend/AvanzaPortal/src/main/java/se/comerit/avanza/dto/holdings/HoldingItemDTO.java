package se.comerit.avanza.dto.holdings;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

// Preserve the existing JSON field names while exposing a typed API response.
public record HoldingItemDTO(
        Long id, String ticker,
        @JsonProperty("instrument_name") String instrumentName,
        BigDecimal quantity,
        @JsonProperty("avg_buy_price") BigDecimal avgBuyPrice,
        String currency,
        @JsonProperty("account_type") String accountType,
        @JsonProperty("account_name") String accountName,
        double currentPrice, double marketValue, double pnl) {
}
