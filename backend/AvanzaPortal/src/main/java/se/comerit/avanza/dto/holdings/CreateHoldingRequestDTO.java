package se.comerit.avanza.dto.holdings;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateHoldingRequestDTO(
                @NotNull @Positive Integer accountId,
                @NotBlank @Size(max = 20) String ticker,
                @NotBlank @Size(max = 100) String instrumentName,
                @NotBlank @DecimalMin(value = "0", inclusive= false) String quantity,
                @NotBlank @DecimalMin(value = "0", inclusive = false) String avgBuyPrice,
                @NotBlank String currency) {
}