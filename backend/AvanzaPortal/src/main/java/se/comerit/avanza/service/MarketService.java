package se.comerit.avanza.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.nativebridge.FxLibrary;

@Service
public class MarketService {

    // The FX library used to fetch foreign exchange rates.
    private final FxLibrary fxLibrary;

    public MarketService(FxLibrary fxLibrary) {
        this.fxLibrary = fxLibrary;
    }

    /**
     * Retrieves the foreign exchange rate for the given currency pair.
     *
     * @param from the base currency
     * @param to   the quote currency
     * @return the FX rate response DTO
     * @throws IllegalArgumentException if the currency pair is unknown
     */
    public FxRateResponseDTO getFx(String from, String to) {
        // Get the current date in epoch seconds at the start of the day (UTC)
        long today = LocalDate.now()
                .atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond();

        // Fetch the FX rate for the given currency pair and date.
        double rate = fxLibrary.fx_rate(from.toUpperCase(), to.toUpperCase(), today);

        // If the rate is negative, it indicates an unknown currency pair.
        if (rate < 0) {
            throw new IllegalArgumentException("Unknown currency pair: " + from + "/" + to);
        }

        // Return the FX rate response DTO with the current date, base currency, quote
        // currency, and rate.
        return new FxRateResponseDTO(
                LocalDate.now().toString(),
                from.toUpperCase(),
                to.toUpperCase(),
                rate);
    }
}
