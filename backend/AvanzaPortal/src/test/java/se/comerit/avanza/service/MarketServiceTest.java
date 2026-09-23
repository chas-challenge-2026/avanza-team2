package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.nativebridge.FxLibrary;

class MarketServiceTest {

    @Test
    void getFxReturnsRateForKnownPair() {
        FxLibrary fxLibrary = mock(FxLibrary.class);
        when(fxLibrary.fx_rate(eq("USD"), eq("SEK"), anyLong())).thenReturn(1.1539);

        MarketService service = new MarketService(fxLibrary);
        FxRateResponseDTO result = service.getFx("usd", "sek");

        assertEquals("USD", result.base());
        assertEquals("SEK", result.quote());
        assertEquals(1.1539, result.rate());
    }

    @Test
    void shouldThrowIllegalArgumentIfRateBelowZero() {
        FxLibrary fxLibrary = mock(FxLibrary.class);
        when(fxLibrary.fx_rate(eq("USD"), eq("SEK"), anyLong())).thenReturn(-1.0);

        MarketService service = new MarketService(fxLibrary);

        try {
            service.getFx("usd", "sek");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown currency pair: usd/sek", e.getMessage());
        }
    }
}
