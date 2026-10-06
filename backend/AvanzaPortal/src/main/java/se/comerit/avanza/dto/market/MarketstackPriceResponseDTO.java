package se.comerit.avanza.dto.market;

import java.math.BigDecimal;
import java.util.List;

public record MarketstackPriceResponseDTO(
                Pagination pagination,
                List<PriceData> data) {
        public record Pagination(
                        int limit,
                        int offset,
                        int count,
                        int total) {
        }

        public record PriceData(
                        String symbol,
                        String exchange,
                        String date,
                        BigDecimal close) {
        }
}
