package se.comerit.avanza.dto.portfolio;

/**
 * AccountAllocationDTO describes one account's Aktier/Fonder split compared to
 * the target the user has set for that account.
 */
public record AccountAllocationDTO(
        Long accountId,
        String accountType,
        String accountName,
        double valueSek,
        double aktierValueSek,
        double fonderValueSek,
        double actualAktierPct,
        double targetAktierPct,
        double driftPct,
        boolean overThreshold) {
}
