package se.comerit.avanza.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.nativebridge.FxLibrary;

@Service
public class MarketService {

    private final FxLibrary fxLibrary;

    public MarketService(FxLibrary fxLibrary) {
        this.fxLibrary = fxLibrary;
    }

    public FxRateResponseDTO getFx(String from, String to) {
        long today = LocalDate.now()
                .atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond();

        double rate = fxLibrary.fx_rate(from.toUpperCase(), to.toUpperCase(), today);

        if (rate < 0) {
            throw new IllegalArgumentException("Unknown currency pair: " + from + "/" + to);
        }

        return new FxRateResponseDTO(
                LocalDate.now().toString(),
                from.toUpperCase(),
                to.toUpperCase(),
                rate);
    }
}
