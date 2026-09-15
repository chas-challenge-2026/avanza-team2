package se.comerit.avanza.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import se.comerit.avanza.dto.market.FxRateResponseDTO;

@Service
public class MarketService {

    // Rest client for making HTTP requests to the Frankfurter API
    private final RestClient restClient;

    // Constructor for initializing the MarketService with the Frankfurter API base
    // URL
    public MarketService(@Value("${frankfurter.api.base-url}") String frankfurterBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(frankfurterBaseUrl).build();
    }

    /**
     * The actual method for fetching the foreign exchange rate from the Frankfurter
     * API.
     * 
     * @param from the base currency code
     * @param to   the quote currency code
     * @return the foreign exchange rate response containing the rate for the given
     *         currency pair
     */
    public FxRateResponseDTO getFx(String from, String to) {
        try {
            // create the GET request to fetch the exchange rate from the Frankfurter API
            return restClient.get()
                    .uri("/v2/rate/{from}/{to}", from, to)
                    .retrieve()
                    .body(FxRateResponseDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Unknown currency pair: " + from + "/" + to, e);
        }
    }
}
