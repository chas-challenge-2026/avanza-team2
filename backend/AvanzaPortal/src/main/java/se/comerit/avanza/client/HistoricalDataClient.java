package se.comerit.avanza.client;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import se.comerit.avanza.dto.market.MarketstackPriceResponseDTO;

/**
 * Client for fetching historical stock price data from the Marketstack API.
 * Implements the StockPriceHistoryClient interface.
 * Provides methods to fetch recent and historical stock prices.
 */
@Component
public class HistoricalDataClient implements StockPriceHistoryClient {

    private final RestClient restClient;
    private final String apiKey;
    private final int lookbackDays;

    public HistoricalDataClient(RestClient.Builder restClientBuilder,
            @Value("${marketstack.base-url}") String baseUrl,
            @Value("${marketstack.api-key}") String apiKey,
            @Value("${marketstack.history.lookback-days:360}") int lookbackDays) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.lookbackDays = lookbackDays;
    }

    /**
     * With this configuration we are able to fetch recent history.
     * Meaning that we are setting up local date range from current time
     * to a date determined by the lookback period specified in the configuration.
     * 
     * @param symbols the list of stock symbols to fetch historical prices for
     * @return the list of recent historical prices for the given symbols
     */
    public List<HistoricalPrice> fetchRecentHistoricalPrices(List<String> symbols) {
        if (lookbackDays <= 0) {
            return List.of();
        }

        LocalDate toDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        LocalDate fromDate = toDate.minusDays(lookbackDays - 1L);
        return fetchHistoricalPrices(symbols, fromDate, toDate);
    }

    /**
     * Fetches historical prices for the given list of symbols within the specified
     * date range from
     * Marketstack API.
     *
     * @param symbols  the list of stock symbols to fetch historical prices for
     * @param fromDate the start date of the historical data range
     * @param toDate   the end date of the historical data range
     * @return the list of historical prices for the given symbols within the
     *         specified date range
     */
    @Override
    public List<HistoricalPrice> fetchHistoricalPrices(
            List<String> symbols,
            LocalDate fromDate,
            LocalDate toDate) {

        // Validate input parameters and API key before making the request.
        if (symbols == null || symbols.isEmpty()
                || fromDate == null
                || toDate == null
                || fromDate.isAfter(toDate)
                || apiKey == null || apiKey.isBlank()) {
            return List.of();
        }

        // Filter out invalid symbols and prepare the list of requested symbols.
        List<String> requestedSymbols = symbols.stream()
                .filter(symbol -> symbol != null && !symbol.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (requestedSymbols.isEmpty()) {
            return List.of();
        }

        // Initialize the list to store historical prices and set the initial offset for
        // pagination.
        List<HistoricalPrice> prices = new ArrayList<>();
        int offset = 0;

        // Begin fetching historical prices from the Marketstack API using pagination.
        try {
            int total;
            do {
                int pageOffset = offset;
                MarketstackPriceResponseDTO response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v2/eod")
                                .queryParam("access_key", apiKey)
                                .queryParam("symbols", String.join(",", requestedSymbols))
                                .queryParam("date_from", fromDate)
                                .queryParam("date_to", toDate)
                                .queryParam("limit", 1000)
                                .queryParam("offset", pageOffset)
                                .build())
                        .retrieve()
                        .body(MarketstackPriceResponseDTO.class);

                // If the response is null or contains no data, break the loop as there are no
                // more prices to fetch.
                if (response == null || response.data() == null || response.data().isEmpty()) {
                    break;
                }

                // Process each price data entry and validate its fields before adding to the
                // list.
                for (MarketstackPriceResponseDTO.PriceData priceData : response.data()) {
                    if (priceData.symbol() == null || priceData.symbol().isBlank()
                            || priceData.date() == null
                            || priceData.close() == null
                            || priceData.close().signum() <= 0) {
                        continue;
                    }

                    // Attempt to parse the date and add the historical price to the list. If
                    // parsing fails, skip this entry.
                    try {
                        LocalDate date = LocalDate.parse(priceData.date().substring(0, 10));
                        prices.add(new HistoricalPrice(priceData.symbol(), date, priceData.close()));
                    } catch (DateTimeParseException | IndexOutOfBoundsException exception) {
                        continue;
                    }
                }

                // Check if the pagination information is available; if not, break the loop.
                if (response.pagination() == null) {
                    break;
                }

                // Update the offset and total count for the next iteration of pagination.
                offset += response.data().size();
                total = response.pagination().total();
            } while (offset < total);

            // Sort the collected historical prices by date and symbol before returning.
            prices.sort(Comparator.comparing(HistoricalPrice::date).thenComparing(HistoricalPrice::symbol));
            return prices;
        } catch (RestClientException exception) {
            return List.of();
        }
    }
}
