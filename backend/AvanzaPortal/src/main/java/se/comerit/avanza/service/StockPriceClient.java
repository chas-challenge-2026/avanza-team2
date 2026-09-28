package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Optional;

public interface StockPriceClient {

    Optional<BigDecimal> fetchPrince(String symbol, String excange);
    
}
