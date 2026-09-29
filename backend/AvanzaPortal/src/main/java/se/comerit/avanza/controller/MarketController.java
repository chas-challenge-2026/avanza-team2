package se.comerit.avanza.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.comerit.avanza.dto.market.FxRateResponseDTO;
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
        try {
            return ResponseEntity.ok(marketService.getFx(from, to));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
