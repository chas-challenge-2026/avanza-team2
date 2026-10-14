package se.comerit.avanza.dto.market;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BacktestRequestDTO(
        @NotEmpty List<@NotBlank String> tickers,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank @Pattern(regexp = "BUY_HOLD|REBALANCE_MONTHLY") String strategy) {
}