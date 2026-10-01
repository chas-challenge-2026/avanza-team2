package se.comerit.avanza.dto.market;

import java.math.BigDecimal;
import java.util.List;

public record MarketstackPriceResponseDTO(
        List<PriceData> data
) {
    public record PriceData(
            String symbol,
            String exchange,
            BigDecimal close
    ) {
    }
}
