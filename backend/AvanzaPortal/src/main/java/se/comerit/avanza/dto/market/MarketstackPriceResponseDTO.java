package se.comerit.avanza.dto.market;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data transfer object representing the response from the Marketstack API for
 * historical stock prices.
 * 
 * @param pagination the pagination information for the response
 * @param data       the list of historical price data entries
 */
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
