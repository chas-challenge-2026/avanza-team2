package se.comerit.avanza.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MarketstackStockPriceClientTest {

    private static final String BASE_URL = "https://api.marketstack.com";

    @Test
    void fetchesLatestPriceForSymbol() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("/v2/eod/latest?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("access_key", "test-key"))
                .andExpect(queryParam("symbols", "ERIC-B.ST"))
                .andRespond(withSuccess("""
                        { "data": [{ "symbol": "ERIC-B.ST", "close": 92.34 }] }
                        """, MediaType.APPLICATION_JSON));

        MarketstackStockPriceClient client = new MarketstackStockPriceClient(builder, BASE_URL, "test-key");

        assertEquals(Optional.of(new BigDecimal("92.34")), client.fetchPrice("ERIC-B.ST", "XSTO"));
        server.verify();
    }

    @Test
    void fetchesMultiplePricesInOneRequest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        List<String> symbols = List.of("ERIC-B.ST", "AAPL");
        server.expect(requestTo(containsString("/v2/eod/latest?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("access_key", "test-key"))
                .andExpect(queryParam("symbols", "ERIC-B.ST,AAPL"))
                .andRespond(withSuccess("""
                        {
                            "data": [
                                { "symbol": "AAPL", "close": 256.18 },
                                { "symbol": "ERIC-B.ST", "close": 92.34 },
                                { "symbol": "UNKNOWN", "close": 1.00 },
                                { "symbol": "AAPL", "close": 0 }
                            ]
                        }
                        """, MediaType.APPLICATION_JSON));

        MarketstackStockPriceClient client = new MarketstackStockPriceClient(builder, BASE_URL, "test-key");

        assertEquals(Map.of(
                "ERIC-B.ST", new BigDecimal("92.34"),
                "AAPL", new BigDecimal("256.18")), client.fetchPrices(symbols));
        server.verify();
    }

    @Test
    void returnsEmptyWhenResponseHasNoUsablePrice() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        List<String> responses = List.of(
                "{ \"data\": [] }",
                "{ \"data\": [{ \"close\": null }] }",
                "{ \"data\": [{ \"close\": 0 }] }",
                "{ \"data\": [{ \"close\": -1 }] }");

        for (String response : responses) {
            server.expect(requestTo(org.hamcrest.Matchers.containsString("/v2/eod/latest?")))
                    .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        }
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v2/eod/latest?")))
                .andRespond(withNoContent());

        MarketstackStockPriceClient client = new MarketstackStockPriceClient(builder, BASE_URL, "test-key");

        for (int i = 0; i < responses.size() + 1; i++) {
            assertTrue(client.fetchPrice("ERIC-B.ST", "XSTO").isEmpty());
        }
        server.verify();
    }

    @Test
    void skipsRequestWhenRequiredInputIsMissing() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MarketstackStockPriceClient client = new MarketstackStockPriceClient(builder, BASE_URL, "test-key");

        assertTrue(client.fetchPrice(null, "XSTO").isEmpty());
        assertTrue(client.fetchPrice(" ", "XSTO").isEmpty());
        assertTrue(client.fetchPrice("ERIC-B.ST", null).isEmpty());
        assertTrue(client.fetchPrice("ERIC-B.ST", " ").isEmpty());

        MarketstackStockPriceClient clientWithoutApiKey = new MarketstackStockPriceClient(
                RestClient.builder(), BASE_URL, " ");
        assertTrue(clientWithoutApiKey.fetchPrice("ERIC-B.ST", "XSTO").isEmpty());
        server.verify();
    }

    @Test
    void returnsEmptyWhenMarketstackRequestFails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v2/eod/latest?")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        MarketstackStockPriceClient client = new MarketstackStockPriceClient(builder, BASE_URL, "test-key");

        assertTrue(client.fetchPrice("ERIC-B.ST", "XSTO").isEmpty());
        server.verify();
    }
}