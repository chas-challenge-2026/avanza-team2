package se.comerit.avanza.client;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HistoricalDataClientTest {

  private static final String BASE_URL = "https://api.marketstack.com";
  private static final LocalDate REQUESTED_DATE = LocalDate.of(2025, 10, 6);
  private static final List<String> SYMBOLS = List.of("ERIC-B.ST", "VOLV-B.ST");

  @Test
  void fetchesBatchOfHistoricalPricesForDateRange() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(containsString("/v2/eod?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(queryParam("symbols", "ERIC-B.ST,VOLV-B.ST"))
        .andExpect(queryParam("date_from", "2025-10-06"))
        .andExpect(queryParam("date_to", "2025-10-07"))
        .andExpect(queryParam("offset", "0"))
        .andRespond(withSuccess("""
            {
              "pagination": { "limit": 2, "offset": 0, "count": 2, "total": 3 },
              "data": [
                {
                  "symbol": "ERIC-B.ST",
                  "exchange": "XSTO",
                  "date": "2025-10-06T00:00:00+0000",
                  "close": 92.34
                },
                {
                  "symbol": "ERIC-B.ST",
                  "exchange": "XSTO",
                  "date": "2025-10-07T00:00:00+0000",
                  "close": 93.12
                }
              ]
            }
            """, MediaType.APPLICATION_JSON));
    server.expect(requestTo(containsString("/v2/eod?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(queryParam("symbols", "ERIC-B.ST,VOLV-B.ST"))
        .andExpect(queryParam("offset", "2"))
        .andRespond(withSuccess("""
            {
              "pagination": { "limit": 2, "offset": 2, "count": 1, "total": 3 },
              "data": [
                {
                  "symbol": "VOLV-B.ST",
                  "exchange": "XSTO",
                  "date": "2025-10-06T00:00:00+0000",
                  "close": 280.50
                }
              ]
            }
            """, MediaType.APPLICATION_JSON));

    HistoricalDataClient client = new HistoricalDataClient(builder, BASE_URL, "test-key", 360);

    List<StockPriceHistoryClient.HistoricalPrice> result = client.fetchHistoricalPrices(
        SYMBOLS,
        REQUESTED_DATE,
        REQUESTED_DATE.plusDays(1));

    assertEquals(List.of(
        new StockPriceHistoryClient.HistoricalPrice("ERIC-B.ST", REQUESTED_DATE, new BigDecimal("92.34")),
        new StockPriceHistoryClient.HistoricalPrice("VOLV-B.ST", REQUESTED_DATE, new BigDecimal("280.50")),
        new StockPriceHistoryClient.HistoricalPrice("ERIC-B.ST", REQUESTED_DATE.plusDays(1),
            new BigDecimal("93.12"))),
        result);
    server.verify();
  }

  @Test
  void fetchesRecentHistoryUsingConfiguredLookback() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    LocalDate toDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
    LocalDate fromDate = toDate.minusDays(359);

    server.expect(requestTo(containsString("/v2/eod?")))
        .andExpect(queryParam("symbols", "ERIC-B.ST,VOLV-B.ST"))
        .andExpect(queryParam("date_from", fromDate.toString()))
        .andExpect(queryParam("date_to", toDate.toString()))
        .andRespond(withSuccess("""
            { "pagination": { "limit": 1000, "offset": 0, "count": 0, "total": 0 }, "data": [] }
            """, MediaType.APPLICATION_JSON));

    HistoricalDataClient client = new HistoricalDataClient(builder, BASE_URL, "test-key", 360);

    assertTrue(client.fetchRecentHistoricalPrices(SYMBOLS).isEmpty());
    server.verify();
  }

  @Test
  void returnsEmptyForMissingSymbolOrDate() {
    HistoricalDataClient client = new HistoricalDataClient(
        RestClient.builder(), BASE_URL, "test-key", 360);

    assertTrue(client.fetchHistoricalPrices(
        List.of(" "),
        REQUESTED_DATE,
        REQUESTED_DATE).isEmpty());

    assertTrue(client.fetchHistoricalPrices(
        SYMBOLS,
        null,
        REQUESTED_DATE).isEmpty());
    assertTrue(client.fetchHistoricalPrices(
        SYMBOLS,
        REQUESTED_DATE,
        null).isEmpty());
    assertTrue(client.fetchHistoricalPrices(
        SYMBOLS,
        REQUESTED_DATE.plusDays(1),
        REQUESTED_DATE).isEmpty());
  }
}