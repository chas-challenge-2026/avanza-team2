package se.comerit.avanza.dto.market;

public record FxRateResponseDTO(
        String date,
        String base,
        String quote,
        double rate) {
}