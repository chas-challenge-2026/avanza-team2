package se.comerit.avanza.dto.targetallocation;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TargetAllocationRequestDTO(

    @NotBlank
    @Size(max = 10)
    String accountType,

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    BigDecimal targetPercentage
) {
}
