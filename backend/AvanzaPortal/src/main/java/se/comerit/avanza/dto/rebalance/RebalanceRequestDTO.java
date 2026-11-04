package se.comerit.avanza.dto.rebalance;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RebalanceRequestDTO(
        @NotNull Long accountId,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal targetAktierPct) {
}
