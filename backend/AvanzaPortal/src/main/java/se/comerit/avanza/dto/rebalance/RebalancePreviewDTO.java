package se.comerit.avanza.dto.rebalance;

import java.math.BigDecimal;
import java.util.List;

public record RebalancePreviewDTO(
        Long accountId,
        BigDecimal totalValue,
        BigDecimal currentAktierPct,
        BigDecimal currentFonderPct,
        BigDecimal targetAktierPct,
        BigDecimal targetFonderPct,
        boolean drifted,
        List<RebalanceTradeDTO> trades,
        // Set when no trades can be made, for example when a category has no holdings.
        String warning) {
}
