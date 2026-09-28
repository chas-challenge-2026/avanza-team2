package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import se.comerit.avanza.dto.market.TwelveDataPriceResponseDTO;


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
    public Optional<BigDecimal> fetchPrice(String symbol, String exchange) {

        if (symbol == null || symbol.isEmpty() || exchange == null || exchange.isEmpty()) {
            return Optional.empty();
        }

        try {
            TwelveDataPriceResponseDTO response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/price")
                        .queryParam("symbol", symbol)
                        .queryParam("exchange", exchange)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .body(TwelveDataPriceResponseDTO.class);

                if(response == null || response.price() == null || response.price().isBlank()) {
                    return Optional.empty();
        }

        BigDecimal price = new BigDecimal(response.price());

        if(price.signum() <= 0) {
            return Optional.empty();
        }

        return Optional.of(price);
    } catch (RestClientException | NumberFormatException exception) {
        return Optional.empty();
    }
        
    }
    
}
