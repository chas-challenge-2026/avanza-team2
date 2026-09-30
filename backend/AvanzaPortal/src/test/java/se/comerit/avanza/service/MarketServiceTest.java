package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import se.comerit.avanza.nativebridge.FxLibrary;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketServiceTest {

    @Test
    void getFxReturnsRateForKnownPair() {
        FxLibrary fxLibrary = mock(FxLibrary.class);
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        when(fxLibrary.fx_rate(eq("USD"), eq("SEK"), anyLong())).thenReturn(1.1539);

        MarketService service = new MarketService(fxLibrary, stockPriceClient);
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

        MarketService service = new MarketService(mock(FxLibrary.class), stockPriceClient);
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

        MarketService service = new MarketService(mock(FxLibrary.class), stockPriceClient);
        Optional<BigDecimal> firstResult = service.getPrice("AAPL");
        Optional<BigDecimal> secondResult = service.getPrice("AAPL");

        assertEquals(Optional.of(expectedPrice), firstResult);
        assertEquals(Optional.of(expectedPrice), secondResult);
        verify(stockPriceClient, times(1)).fetchPrice("AAPL", "XNAS");
    }

    @Test
    void shouldReturnEmptyForUnknownTicker() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        MarketService service = new MarketService(mock(FxLibrary.class), stockPriceClient);

        Optional<BigDecimal> result = service.getPrice("UNKNOWN");

        assertTrue(result.isEmpty());
        verifyNoInteractions(stockPriceClient);
    }

    @Test
    void shouldReturnEmptyWhenProviderHasNoPrice() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        when(stockPriceClient.fetchPrice("SAND.ST", "XSTO"))
                .thenReturn(Optional.empty());

        MarketService service = new MarketService(mock(FxLibrary.class), stockPriceClient);
        Optional<BigDecimal> result = service.getPrice("SAND");

        assertTrue(result.isEmpty());
        verify(stockPriceClient).fetchPrice("SAND.ST", "XSTO");
    }

    @Test
    void shouldReturnEmptyForBlankTicker() {
        StockPriceClient stockPriceClient = mock(StockPriceClient.class);
        MarketService service = new MarketService(mock(FxLibrary.class), stockPriceClient);

        Optional<BigDecimal> result = service.getPrice(" ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(stockPriceClient);
    }
}
