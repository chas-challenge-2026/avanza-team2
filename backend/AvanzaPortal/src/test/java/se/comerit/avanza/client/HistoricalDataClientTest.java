package se.comerit.avanza.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HistoricalDataClientTest {

    private static final String BASE_URL = "https://api.marketstack.com";
    private static final LocalDate REQUESTED_DATE = LocalDate.of(2025, 10, 6);
    private static final String HISTORICAL_URL = BASE_URL
            + "/v2/eod?access_key=test-key&symbols=ERIC-B.ST&date_from=2025-10-06&date_to=2025-10-06";

    @Test
    void fetchesClosingPriceForRequestedDate() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(HISTORICAL_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {
                              "symbol": "ERIC-B.ST",
                              "exchange": "XSTO",
                              "close": 92.34
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        HistoricalDataClient client = new HistoricalDataClient(builder, BASE_URL, "test-key");

        Optional<BigDecimal> result = client.fetchHistoricalPrice("ERIC-B.ST", "XSTO", REQUESTED_DATE);

        assertEquals(Optional.of(new BigDecimal("92.34")), result);
        server.verify();
    }

    @Test
    void returnsEmptyForMissingSymbolOrDate() {
        HistoricalDataClient client = new HistoricalDataClient(
                RestClient.builder(), BASE_URL, "test-key");

        assertTrue(client.fetchHistoricalPrice(" ", "XSTO", REQUESTED_DATE).isEmpty());
        assertTrue(client.fetchHistoricalPrice("ERIC-B.ST", "XSTO", null).isEmpty());
    }
}