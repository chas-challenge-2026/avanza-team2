package se.comerit.avanza.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

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

    public Optional<BigDecimal> fetchHistoricalPrice(String symbol, String exchange, LocalDate date) {

        if (symbol == null || symbol.isBlank()
                || exchange == null || exchange.isBlank()
                || date == null
                || apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            MarketstackPriceResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/eod")
                            .queryParam("access_key", apiKey)
                            .queryParam("symbols", symbol)
                            .queryParam("date_from", date)
                            .queryParam("date_to", date)
                            .build())
                    .retrieve()
                    .body(MarketstackPriceResponseDTO.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                return Optional.empty();
            }

            BigDecimal price = response.data().get(0).close();

            if (price == null || price.signum() <= 0) {
                return Optional.empty();
            }

            return Optional.of(price);
        } catch (RestClientException exception) {
            return Optional.empty();
        }
    }
}
