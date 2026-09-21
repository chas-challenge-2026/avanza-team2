package se.comerit.avanza.dto.portfolio;

public record SharpeRatioResponseDTO(
                double sharpeRatio,
                double riskFreeRate,
                int yearFreq,
                double volatility) {
}
