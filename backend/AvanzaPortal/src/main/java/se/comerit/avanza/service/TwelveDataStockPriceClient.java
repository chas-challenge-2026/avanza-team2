package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component 
public class TwelveDataStockPriceClient implements StockPriceClient {

    private final RestClient restClient;
    private final String apiKey;

    public TwelveDataStockPriceClient(RestClient.Builder restClientBuilder, 
        @Value("${twelve-data.base-url}") String baseUrl, @Value("${twelve-data.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();

        this.apiKey = apiKey;
    }

    @Override
    public Optional<BigDecimal> fetchPrince(String symbol, String exchange) {
        // Implement the logic to fetch the price from TwelveData API
        // For now, return an empty Optional as a placeholder
        return Optional.empty();
    }
    
}
