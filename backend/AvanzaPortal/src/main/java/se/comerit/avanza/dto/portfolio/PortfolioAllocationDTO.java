package se.comerit.avanza.dto.portfolio;

/**
 * PortfolioAllocationDTO is the Aktier/Fonder split across the whole portfolio.
 */
public record PortfolioAllocationDTO(
        double aktierPct,
        double fonderPct) {
}
