package se.comerit.avanza.dto.portfolio;

/**
 * AllocationRowDTO is a record that represents a row in the allocation view of
 * a user's portfolio.
 */
public record AllocationRowDTO(
        String accountType,
        double actual,
        double target,
        double drift,
        boolean overTreshold) {
}
