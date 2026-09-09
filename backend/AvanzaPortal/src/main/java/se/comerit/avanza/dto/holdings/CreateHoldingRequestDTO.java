package se.comerit.avanza.dto.holdings;

public record CreateHoldingRequestDTO(
                Integer accountId,
                String ticker,
                String instrumentName,
                String quantity,
                String avgBuyPrice,
                String currency) {
}