package se.comerit.avanza.controller;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.comerit.avanza.entity.User;
import se.comerit.avanza.service.MarketService;
import se.comerit.avanza.service.PortfolioService;
import se.comerit.avanza.service.PortfolioSnapshotService;

/**
 * We use a snapshot controller becouse snapshots is an HTTP operation initiated
 * by the frontend.
 * It handles requests to capture portfolio snapshots.
 * PortfolioSnapshotController
 */
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioSnapshotController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioSnapshotController.class);

    private final PortfolioService portfolioService;
    private final PortfolioSnapshotService snapshotService;
    private final MarketService marketService;

    public PortfolioSnapshotController(
            PortfolioService portfolioService,
            PortfolioSnapshotService snapshotService,
            MarketService marketService) {
        this.portfolioService = portfolioService;
        this.snapshotService = snapshotService;
        this.marketService = marketService;
    }

    /**
     * Capture snapshot for the current user.
     * 
     * @param userName the current user
     * @return number of saved rows
     */
    @PostMapping("/snapshot")
    public ResponseEntity<Integer> captureSnapshot(@AuthenticationPrincipal String userName) {
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> user = portfolioService.findByEmail(userName);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            marketService.importHistoricalData();
        } catch (RuntimeException exception) {
            logger.warn("Historical import failed during page-load snapshot; using stored prices", exception);
        }

        LocalDate throughDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        int savedRows = snapshotService.captureLatestSnapshotForUser(user.get().getId(), throughDate);
        return ResponseEntity.ok(savedRows);
    }

    /**
     * Capture snapshot for a given date.
     * 
     * @param date the date to capture snapshot for
     * @return number of saved rows
     */
    @PostMapping("/snapshots")
    public ResponseEntity<Integer> captureSnapshot(
            @AuthenticationPrincipal String userName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> user = portfolioService.findByEmail(userName);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            return ResponseEntity.ok(snapshotService.captureSnapshotForUser(user.get().getId(), date));
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}