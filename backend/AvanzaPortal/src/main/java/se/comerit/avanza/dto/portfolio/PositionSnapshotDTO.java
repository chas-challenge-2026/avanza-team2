package se.comerit.avanza.dto.portfolio;

import java.math.BigDecimal;

/**
 * PositionSnapshotDTO is record for position data. This data is used for
 * snapshots of account that will be used afterwards to calculate share values
 * 
 * @param accountId   account id
 * @param ticker      stock ticker
 * @param currency    currency code
 * @param accountType type of account
 * @param quantity    amount held
 */
public record PositionSnapshotDTO(
        Long accountId,
        String ticker,
        String currency,
        String accountType,
        BigDecimal quantity) {
}
