package se.comerit.avanza.dto.targetallocation;

import java.math.BigDecimal;

public record TargetAllocationResponseDTO(
        Long id,
        Long accountId,
        String accountType,
        BigDecimal targetPercentage) {
}
