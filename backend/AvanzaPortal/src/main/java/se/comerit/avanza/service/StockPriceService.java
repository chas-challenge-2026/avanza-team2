package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class StockPriceService {

    private static final Map<String, MarketSymbol> MARKET_SYMBOLS = Map.of(
        "ERIC-B", new MarketSymbol("ERIC.B", "OMX"),
        "VOLV-B", new MarketSymbol("VOLV.B", "OMX"),
        "AAPL", new MarketSymbol("AAPL", "NASDAQ"),
        "SWED-A", new MarketSymbol("SWED.A", "OMX"),
        "SAND", new MarketSymbol("SAND", "OMX")
    );

    private final StockPriceClient stockPriceClient;
    private final Map<String, BigDecimal> priceCache = new ConcurrentHashMap<>();

    public StockPriceService(StockPriceClient stockPriceClient) {
        this.stockPriceClient = stockPriceClient;
    }

    public Optional<BigDecimal> getPrice(String ticker) {

        if (ticker == null || ticker.isBlank()) {
            return Optional.empty();
        }

        String normalizedTicker = ticker.trim().toUpperCase();
        MarketSymbol marketSymbol = MARKET_SYMBOLS.get(normalizedTicker);

        if (marketSymbol == null) {
            return Optional.empty();
        }

        // Check cache first
        BigDecimal cachedPrice = priceCache.get(normalizedTicker);

        if (cachedPrice != null) {
            return Optional.of(cachedPrice);
        }

        Optional<BigDecimal> fetchedPrice = stockPriceClient.fetchPrice(marketSymbol.symbol(), marketSymbol.exchange());

        fetchedPrice.ifPresent(price -> priceCache.put(normalizedTicker, price));

        return fetchedPrice;
        


        
    }

    private record MarketSymbol(String symbol, String exchange) {
    }




    
}
 