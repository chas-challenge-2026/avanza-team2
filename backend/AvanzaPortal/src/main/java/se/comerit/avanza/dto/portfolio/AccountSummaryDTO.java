package se.comerit.avanza.dto.portfolio;

/**
 * AccountSummaryDTO is a record that represents a summary of a user's account
 * in their portfolio.
 */
public record AccountSummaryDTO(
        Long id,
        String accountType,
        String accountName,
        String currency,
        double totalValueSek) {
}
