package se.comerit.avanza.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.service.MarketService;

@RestController
public class MarketController {

    // Market service for handling market-related requests
    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    /**
     * Fetches the foreign exchange rate for the given currency pair.
     * 
     * @param from
     * @param to
     * @return
     */
    @GetMapping("/fx/{from}/{to}")
    public ResponseEntity<FxRateResponseDTO> getFx(@PathVariable String from, @PathVariable String to) {
        try {
            return ResponseEntity.ok(marketService.getFx(from, to));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
