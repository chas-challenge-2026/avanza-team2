package se.comerit.avanza.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import se.comerit.avanza.dto.market.FxRateResponseDTO;

class MarketServiceTest {

    @Test
    void getFxReturnsRateFromFrankfurter() {
        MarketService service = new MarketService("https://api.frankfurter.dev");
        FxRateResponseDTO result = service.getFx("eur", "usd");
        assertNotNull(result);
        assertTrue(result.rate() > 0);
    }
}