package se.comerit.avanza.client;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        return Optional.ofNullable(fetchPrices(List.of(symbol)).get(symbol));
    }

    @Override
    public Map<String, BigDecimal> fetchPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty() || apiKey == null || apiKey.isBlank()) {
            return Map.of();
        }

        List<String> requestedSymbols = symbols.stream()
                .filter(symbol -> symbol != null && !symbol.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (requestedSymbols.isEmpty()) {
            return Map.of();
        }

        try {
            MarketstackPriceResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/eod/latest")
                            .queryParam("access_key", apiKey)
                            .queryParam("symbols", String.join(",", requestedSymbols))
                            .build())
                    .retrieve()
                    .body(MarketstackPriceResponseDTO.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                return Map.of();
            }

            Set<String> requestedSymbolSet = Set.copyOf(requestedSymbols);
            Map<String, BigDecimal> prices = new HashMap<>();
            for (MarketstackPriceResponseDTO.PriceData priceData : response.data()) {
                if (priceData.symbol() == null || priceData.close() == null
                        || priceData.close().signum() <= 0) {
                    continue;
                }

                String returnedSymbol = priceData.symbol().trim();
                if (requestedSymbolSet.contains(returnedSymbol)) {
                    prices.put(returnedSymbol, priceData.close());
                }
            }

            return Map.copyOf(prices);
        } catch (RestClientException exception) {
            return Map.of();
        }
    }
}
