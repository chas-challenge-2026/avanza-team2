package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockPriceServiceTest {

    @Mock
    private StockPriceClient stockPriceClient;

    @InjectMocks
    private StockPriceService stockPriceService;

    @Test
    void shouldFetchPriceForSupportedTicker() {
        BigDecimal expectedPrice = new BigDecimal("74.20");

        when(stockPriceClient.fetchPrice("ERIC-B.ST", "XSTO"))
                .thenReturn(Optional.of(expectedPrice));

        Optional<BigDecimal> result =
                stockPriceService.getPrice("ERIC-B");

        assertEquals(Optional.of(expectedPrice), result);

        verify(stockPriceClient)
                .fetchPrice("ERIC-B.ST", "XSTO");
    }

    @Test
    void shouldUseCachedPriceOnSecondRequest() {
        BigDecimal expectedPrice = new BigDecimal("339.73");

        when(stockPriceClient.fetchPrice("AAPL", "XNAS"))
                .thenReturn(Optional.of(expectedPrice));

        Optional<BigDecimal> firstResult =
                stockPriceService.getPrice("AAPL");

        Optional<BigDecimal> secondResult =
                stockPriceService.getPrice("AAPL");

        assertEquals(Optional.of(expectedPrice), firstResult);
        assertEquals(Optional.of(expectedPrice), secondResult);

        verify(stockPriceClient, times(1))
                .fetchPrice("AAPL", "XNAS");
    }

    @Test
    void shouldReturnEmptyForUnknownTicker() {
        Optional<BigDecimal> result =
                stockPriceService.getPrice("UNKNOWN");

        assertTrue(result.isEmpty());
        verifyNoInteractions(stockPriceClient);
    }

    @Test
    void shouldReturnEmptyWhenProviderHasNoPrice() {
        when(stockPriceClient.fetchPrice("SAND.ST", "XSTO"))
                .thenReturn(Optional.empty());

        Optional<BigDecimal> result =
                stockPriceService.getPrice("SAND");

        assertTrue(result.isEmpty());

        verify(stockPriceClient)
                .fetchPrice("SAND.ST", "XSTO");
    }

    @Test
    void shouldReturnEmptyForBlankTicker() {
        Optional<BigDecimal> result =
                stockPriceService.getPrice(" ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(stockPriceClient);
    }
}
