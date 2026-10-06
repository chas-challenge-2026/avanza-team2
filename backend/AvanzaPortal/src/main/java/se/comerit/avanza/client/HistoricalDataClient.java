package se.comerit.avanza.client;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import se.comerit.avanza.dto.market.MarketstackPriceResponseDTO;

@Component
public class HistoricalDataClient implements StockPriceHistoryClient {

    private final RestClient restClient;
    private final String apiKey;

    public HistoricalDataClient(RestClient.Builder restClientBuilder,
            @Value("${marketstack.base-url}") String baseUrl,
            @Value("${marketstack.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public List<HistoricalPrice> fetchHistoricalPrices(
            String symbol,
            String exchange,
            LocalDate fromDate,
            LocalDate toDate) {

        if (symbol == null || symbol.isBlank()
                || exchange == null || exchange.isBlank()
                || fromDate == null
                || toDate == null
                || fromDate.isAfter(toDate)
                || apiKey == null || apiKey.isBlank()) {
            return List.of();
        }

        try {
            MarketstackPriceResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/eod")
                            .queryParam("access_key", apiKey)
                            .queryParam("symbols", symbol)
                            .queryParam("date_from", fromDate)
                            .queryParam("date_to", toDate)
                            .queryParam("limit", 1000)
                            .build())
                    .retrieve()
                    .body(MarketstackPriceResponseDTO.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                return List.of();
            }

            List<HistoricalPrice> prices = new ArrayList<>();
            for (MarketstackPriceResponseDTO.PriceData priceData : response.data()) {
                if (priceData.date() == null
                        || priceData.close() == null
                        || priceData.close().signum() <= 0) {
                    continue;
                }

                try {
                    LocalDate date = LocalDate.parse(priceData.date().substring(0, 10));
                    prices.add(new HistoricalPrice(symbol, date, priceData.close()));
                } catch (DateTimeParseException | IndexOutOfBoundsException exception) {
                    continue;
                }
            }

            return prices;
        } catch (RestClientException exception) {
            return List.of();
        }
    }
}
