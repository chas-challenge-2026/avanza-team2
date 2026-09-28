package se.comerit.avanza.dto.holdings;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HoldingAccountDTO(
        Long id,
        @JsonProperty("account_type") String accountType,
        @JsonProperty("account_name") String accountName) {
}
