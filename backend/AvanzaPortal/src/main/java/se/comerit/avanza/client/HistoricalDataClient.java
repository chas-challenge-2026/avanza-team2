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

    public List<HistoricalPrice> fetchRecentHistoricalPrices(List<String> symbols) {
        if (lookbackDays <= 0) {
            return List.of();
        }

        LocalDate toDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        LocalDate fromDate = toDate.minusDays(lookbackDays - 1L);
        return fetchHistoricalPrices(symbols, fromDate, toDate);
    }

    @Override
    public List<HistoricalPrice> fetchHistoricalPrices(
            List<String> symbols,
            LocalDate fromDate,
            LocalDate toDate) {

        if (symbols == null || symbols.isEmpty()
                || fromDate == null
                || toDate == null
                || fromDate.isAfter(toDate)
                || apiKey == null || apiKey.isBlank()) {
            return List.of();
        }

        List<String> requestedSymbols = symbols.stream()
                .filter(symbol -> symbol != null && !symbol.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (requestedSymbols.isEmpty()) {
            return List.of();
        }

        List<HistoricalPrice> prices = new ArrayList<>();
        int offset = 0;
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

                if (response == null || response.data() == null || response.data().isEmpty()) {
                    break;
                }

                for (MarketstackPriceResponseDTO.PriceData priceData : response.data()) {
                    if (priceData.symbol() == null || priceData.symbol().isBlank()
                            || priceData.date() == null
                            || priceData.close() == null
                            || priceData.close().signum() <= 0) {
                        continue;
                    }

                    try {
                        LocalDate date = LocalDate.parse(priceData.date().substring(0, 10));
                        prices.add(new HistoricalPrice(priceData.symbol(), date, priceData.close()));
                    } catch (DateTimeParseException | IndexOutOfBoundsException exception) {
                        continue;
                    }
                }

                if (response.pagination() == null) {
                    break;
                }

                offset += response.data().size();
                total = response.pagination().total();
            } while (offset < total);

            prices.sort(Comparator.comparing(HistoricalPrice::date).thenComparing(HistoricalPrice::symbol));
            return prices;
        } catch (RestClientException exception) {
            return List.of();
        }
    }
}
