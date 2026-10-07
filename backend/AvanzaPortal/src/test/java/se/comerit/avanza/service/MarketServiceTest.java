package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import se.comerit.avanza.client.HistoricalPriceClient;
import se.comerit.avanza.client.StockPriceClient;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.nativebridge.FxLibrary;
import se.comerit.avanza.repository.HistoricalRepository;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketServiceTest {

    private static final List<String> HISTORICAL_TICKERS = List.of(
            "ERIC-B", "VOLV-B", "AAPL", "SWED-A", "SAND");

    @Test
    void getFxReturnsRateForKnownPair() {
        FxLibrary fxLibrary = mock(FxLibrary.class);
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        when(fxLibrary.fx_rate(eq("USD"), eq("SEK"), anyLong())).thenReturn(1.1539);

        MarketService service = createService(fxLibrary, stockPriceClient);
        FxRateResponseDTO result = service.getFx("usd", "sek");

        assertEquals("USD", result.base());
        assertEquals("SEK", result.quote());
        assertEquals(1.1539, result.rate());
    }

    @Test
    void shouldFetchPriceForSupportedTicker() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        BigDecimal expectedPrice = new BigDecimal("74.20");
        when(stockPriceClient.fetchPrice("ERIC-B.ST", "XSTO"))
                .thenReturn(Optional.of(expectedPrice));

        MarketService service = createService(mock(FxLibrary.class), stockPriceClient);
        Optional<BigDecimal> result = service.getPrice("ERIC-B");

        assertEquals(Optional.of(expectedPrice), result);
        verify(stockPriceClient).fetchPrice("ERIC-B.ST", "XSTO");
    }

    @Test
    void shouldUseCachedPriceOnSecondRequest() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        BigDecimal expectedPrice = new BigDecimal("339.73");
        when(stockPriceClient.fetchPrice("AAPL", "XNAS"))
                .thenReturn(Optional.of(expectedPrice));

        MarketService service = createService(mock(FxLibrary.class), stockPriceClient);
        Optional<BigDecimal> firstResult = service.getPrice("AAPL");
        Optional<BigDecimal> secondResult = service.getPrice("AAPL");

        assertEquals(Optional.of(expectedPrice), firstResult);
        assertEquals(Optional.of(expectedPrice), secondResult);
        verify(stockPriceClient, times(1)).fetchPrice("AAPL", "XNAS");
    }

    @Test
    void shouldFetchNewPriceAfterCachedPriceExpires() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        BigDecimal cachedPrice = new BigDecimal("339.73");
        BigDecimal refreshedPrice = new BigDecimal("341.25");
        when(stockPriceClient.fetchPrice("AAPL", "XNAS"))
                .thenReturn(Optional.of(cachedPrice))
                .thenReturn(Optional.of(refreshedPrice));
        MutableClock clock = new MutableClock(Instant.parse("2026-10-01T10:00:00Z"));

        MarketService service = createService(mock(FxLibrary.class), stockPriceClient, clock);

        assertEquals(Optional.of(cachedPrice), service.getPrice("AAPL"));
        clock.advance(Duration.ofMinutes(6));
        assertEquals(Optional.of(refreshedPrice), service.getPrice("AAPL"));

        verify(stockPriceClient, times(2)).fetchPrice("AAPL", "XNAS");
    }

    @Test
    void shouldReturnEmptyWhenRefreshingExpiredPriceFails() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        BigDecimal cachedPrice = new BigDecimal("339.73");
        when(stockPriceClient.fetchPrice("AAPL", "XNAS"))
                .thenReturn(Optional.of(cachedPrice))
                .thenReturn(Optional.empty());
        MutableClock clock = new MutableClock(Instant.parse("2026-10-01T10:00:00Z"));

        MarketService service = createService(mock(FxLibrary.class), stockPriceClient, clock);

        assertEquals(Optional.of(cachedPrice), service.getPrice("AAPL"));
        clock.advance(Duration.ofMinutes(6));
        assertTrue(service.getPrice("AAPL").isEmpty());

        verify(stockPriceClient, times(2)).fetchPrice("AAPL", "XNAS");
    }

    @Test
    void shouldReturnEmptyForUnknownTicker() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        MarketService service = createService(mock(FxLibrary.class), stockPriceClient);

        Optional<BigDecimal> result = service.getPrice("UNKNOWN");

        assertTrue(result.isEmpty());
        verifyNoInteractions(stockPriceClient);
    }

    @Test
    void shouldReturnEmptyWhenProviderHasNoPrice() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        when(stockPriceClient.fetchPrice("SAND.ST", "XSTO"))
                .thenReturn(Optional.empty());

        MarketService service = createService(mock(FxLibrary.class), stockPriceClient);
        Optional<BigDecimal> result = service.getPrice("SAND");

        assertTrue(result.isEmpty());
        verify(stockPriceClient).fetchPrice("SAND.ST", "XSTO");
    }

    @Test
    void shouldReturnEmptyForBlankTicker() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        MarketService service = createService(mock(FxLibrary.class), stockPriceClient);

        Optional<BigDecimal> result = service.getPrice(" ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(stockPriceClient);
    }

    @Test
    void shouldImportInitialLookbackForSupportedTickers() {
        LocalDate toDate = LocalDate.of(2026, 10, 6);
        LocalDate fromDate = toDate.minusDays(359);
        HistoricalPriceClient historyClient = mock(HistoricalPriceClient.class);
        HistoricalRepository historicalRepository = mock(HistoricalRepository.class);
        List<HistoricalPriceClient.HistoricalPrice> fetchedPrices = List.of(
                new HistoricalPriceClient.HistoricalPrice(
                        "ERIC-B", fromDate, new BigDecimal("74.200000")),
                new HistoricalPriceClient.HistoricalPrice(
                        "AAPL", toDate, new BigDecimal("256.180000")));
        when(historyClient.fetchHistoricalPrices(HISTORICAL_TICKERS, fromDate, toDate))
                .thenReturn(fetchedPrices);

        MarketService service = createService(
                mock(FxLibrary.class), mock(StockPriceClient.class), historicalRepository,
                historyClient, Clock.fixed(Instant.parse("2026-10-07T10:00:00Z"), ZoneOffset.UTC), 360);

        assertEquals(2, service.importHistoricalData());
        verify(historyClient).fetchHistoricalPrices(HISTORICAL_TICKERS, fromDate, toDate);
        verify(historicalRepository).saveAll(argThat(rows -> {
            List<HistoricalStockPrice> savedRows = new ArrayList<>();
            rows.forEach(savedRows::add);
            return savedRows.size() == 2
                    && savedRows.stream().anyMatch(price -> price.getTicker().equals("ERIC-B")
                            && price.getPriceDate().equals(fromDate)
                            && price.getClosePrice().equals(new BigDecimal("74.200000")))
                    && savedRows.stream().anyMatch(price -> price.getTicker().equals("AAPL")
                            && price.getPriceDate().equals(toDate));
        }));
    }

    @Test
    void shouldImportIncrementallyFromEarliestTickerGap() {
        LocalDate toDate = LocalDate.of(2026, 10, 6);
        LocalDate fromDate = LocalDate.of(2026, 10, 1);
        HistoricalRepository historicalRepository = mock(HistoricalRepository.class);
        HistoricalPriceClient historyClient = mock(HistoricalPriceClient.class);
        for (String ticker : HISTORICAL_TICKERS) {
            LocalDate latestDate = ticker.equals("ERIC-B")
                    ? LocalDate.of(2026, 9, 30)
                    : LocalDate.of(2026, 10, 5);
            when(historicalRepository.findFirstByTickerOrderByPriceDateDesc(ticker))
                    .thenReturn(Optional.of(new HistoricalStockPrice(
                            ticker, latestDate, new BigDecimal("100.00"))));
        }
        when(historyClient.fetchHistoricalPrices(HISTORICAL_TICKERS, fromDate, toDate))
                .thenReturn(List.of(
                        new HistoricalPriceClient.HistoricalPrice(
                                "ERIC-B", LocalDate.of(2026, 9, 30), new BigDecimal("70.00")),
                        new HistoricalPriceClient.HistoricalPrice(
                                "ERIC-B", fromDate, new BigDecimal("71.00")),
                        new HistoricalPriceClient.HistoricalPrice(
                                "VOLV-B", LocalDate.of(2026, 10, 5), new BigDecimal("270.00")),
                        new HistoricalPriceClient.HistoricalPrice(
                                "VOLV-B", toDate, new BigDecimal("271.00")),
                        new HistoricalPriceClient.HistoricalPrice(
                                "UNKNOWN", LocalDate.of(2026, 10, 2), new BigDecimal("1.00"))));

        MarketService service = createService(
                mock(FxLibrary.class), mock(StockPriceClient.class), historicalRepository,
                historyClient, Clock.fixed(Instant.parse("2026-10-07T10:00:00Z"), ZoneOffset.UTC), 360);

        assertEquals(2, service.importHistoricalData());
        verify(historyClient).fetchHistoricalPrices(HISTORICAL_TICKERS, fromDate, toDate);
        verify(historicalRepository).saveAll(argThat(rows -> {
            Set<String> savedKeys = new HashSet<>();
            rows.forEach(price -> savedKeys.add(price.getTicker() + ":" + price.getPriceDate()));
            return savedKeys.equals(Set.of("ERIC-B:2026-10-01", "VOLV-B:2026-10-06"));
        }));
    }

    @Test
    void shouldSkipImportWhenAllTickersAreUpToDate() {
        LocalDate latestDate = LocalDate.of(2026, 10, 6);
        HistoricalRepository historicalRepository = mock(HistoricalRepository.class);
        HistoricalPriceClient historyClient = mock(HistoricalPriceClient.class);
        for (String ticker : HISTORICAL_TICKERS) {
            when(historicalRepository.findFirstByTickerOrderByPriceDateDesc(ticker))
                    .thenReturn(Optional.of(new HistoricalStockPrice(
                            ticker, latestDate, new BigDecimal("100.00"))));
        }

        MarketService service = createService(
                mock(FxLibrary.class), mock(StockPriceClient.class), historicalRepository,
                historyClient, Clock.fixed(Instant.parse("2026-10-07T10:00:00Z"), ZoneOffset.UTC), 360);

        assertEquals(0, service.importHistoricalData());
        verifyNoInteractions(historyClient);
        verify(historicalRepository, never()).saveAll(any());
    }

    private MarketService createService(FxLibrary fxLibrary, StockPriceClient stockPriceClient) {
        return createService(fxLibrary, stockPriceClient, Clock.systemUTC());
    }

    private MarketService createService(
            FxLibrary fxLibrary,
            StockPriceClient stockPriceClient,
            Clock clock) {
        return new MarketService(
                fxLibrary,
                stockPriceClient,
                mock(HistoricalRepository.class),
                mock(HistoricalPriceClient.class),
                clock,
                360);
    }

    private MarketService createService(
            FxLibrary fxLibrary,
            StockPriceClient stockPriceClient,
            HistoricalRepository historicalRepository,
            HistoricalPriceClient historicalDataClient,
            Clock clock,
            int lookbackDays) {
        return new MarketService(
                fxLibrary,
                stockPriceClient,
                historicalRepository,
                historicalDataClient,
                clock,
                lookbackDays);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
