package se.comerit.avanza.dto.targetallocation;

import java.math.BigDecimal;

public record TargetAllocationResponseDTO(
    Long id,
    String accountType,
    BigDecimal targetPercentage
) {
    
}
