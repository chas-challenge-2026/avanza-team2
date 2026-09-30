package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import se.comerit.avanza.dto.market.MarketstackPriceResponseDTO;

/**
 * Retrieves end-of-day stock prices from the Marketstack API.
 */
@Component
public class MarketstackStockPriceClient implements StockPriceClient {

    private final RestClient restClient;
    private final String apiKey;

    public MarketstackStockPriceClient(RestClient.Builder restClientBuilder,
            @Value("${marketstack.base-url}") String baseUrl,
            @Value("${marketstack.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    /**
     * Fetches and validates the latest end-of-day closing price.
     *
     * @param symbol   the Marketstack ticker symbol
     * @param exchange the instrument exchange, used to validate the request input
     * @return a positive closing price, or empty when unavailable
     */
    @Override
    public Optional<BigDecimal> fetchPrice(String symbol, String exchange) {

        if (symbol == null || symbol.isBlank()
                || exchange == null || exchange.isBlank()
                || apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            MarketstackPriceResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/eod/latest")
                            .queryParam("access_key", apiKey)
                            .queryParam("symbols", symbol)
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
