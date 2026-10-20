package se.comerit.avanza.controller;

import java.math.BigDecimal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.comerit.avanza.dto.market.BacktestRequestDTO;
import se.comerit.avanza.dto.market.BacktestResponseDTO;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.service.MarketService;

/**
 * Exposes endpoints for foreign exchange rates and stock prices.
 */
@RestController
@RequestMapping("/api/market")
public class MarketController {

    // Market service for handling market-related requests
    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    /**
     * Retrieves the foreign exchange rate for the given currency pair.
     *
     * @param from from currency
     * @param to   to currency
     * @return the FX rate response entity containing the rate or a bad request
     *         status if the pair is unknown
     */
    @GetMapping("/fx/{from}/{to}")
    public ResponseEntity<FxRateResponseDTO> getFx(@PathVariable String from, @PathVariable String to) {
        return ResponseEntity.ok(marketService.getFx(from, to));
    }

    /**
     * Retrieves the latest available price for a supported ticker.
     *
     * @param ticker the application's ticker symbol
     * @return the price, or not found when it is unsupported or unavailable
     */
    @GetMapping("/price/{ticker}")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable String ticker) {
        return marketService.getPrice(ticker)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found for ticker: " + ticker));
    }

    /**
     * Runs a backtest using historical prices stored by the backend.
     *
     * @param request tickers, date range, and strategy for the analysis
     * @return the backtest response entity containing the analysis results
     */
    @PostMapping("/backtest")
    public ResponseEntity<BacktestResponseDTO> getBacktestAnalysis(
            @Valid @RequestBody BacktestRequestDTO request) {
        try {
            return ResponseEntity.ok(marketService.getBacktestAnalysis(request));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Imports historical stock prices for the supported tickers.
     *
     * @return the number of historical stock prices imported
     */
    @PostMapping("/history/import")
    public ResponseEntity<Integer> importHistoricalPrices() {
        return ResponseEntity.ok(marketService.importHistoricalData());
    }
}
