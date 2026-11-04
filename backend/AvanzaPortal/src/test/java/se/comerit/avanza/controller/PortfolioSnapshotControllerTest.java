package se.comerit.avanza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import se.comerit.avanza.entity.User;
import se.comerit.avanza.service.MarketService;
import se.comerit.avanza.service.PortfolioService;
import se.comerit.avanza.service.PortfolioSnapshotService;

class PortfolioSnapshotControllerTest {

    @Test
    void capturesSnapshotForAuthenticatedUser() {
        PortfolioService portfolioService = mock(PortfolioService.class);
        PortfolioSnapshotService snapshotService = mock(PortfolioSnapshotService.class);
        MarketService marketService = mock(MarketService.class);
        PortfolioSnapshotController controller = new PortfolioSnapshotController(
                portfolioService, snapshotService, marketService);
        User user = new User();
        user.setId(42L);
        when(portfolioService.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        when(snapshotService.captureLatestSnapshotForUser(org.mockito.ArgumentMatchers.eq(42L), any(LocalDate.class)))
                .thenReturn(3);

        ResponseEntity<Integer> response = controller.captureSnapshot("demo@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody());
        verify(marketService).importHistoricalData();
    }

    @Test
    void rejectsUnknownUser() {
        PortfolioService portfolioService = mock(PortfolioService.class);
        PortfolioSnapshotService snapshotService = mock(PortfolioSnapshotService.class);
        MarketService marketService = mock(MarketService.class);
        PortfolioSnapshotController controller = new PortfolioSnapshotController(
                portfolioService, snapshotService, marketService);
        when(portfolioService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseEntity<Integer> response = controller.captureSnapshot("missing@example.com");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void capturesSnapshotForRequestedDate() {
        PortfolioService portfolioService = mock(PortfolioService.class);
        PortfolioSnapshotService snapshotService = mock(PortfolioSnapshotService.class);
        MarketService marketService = mock(MarketService.class);
        PortfolioSnapshotController controller = new PortfolioSnapshotController(
                portfolioService, snapshotService, marketService);
        User user = new User();
        user.setId(7L);
        LocalDate date = LocalDate.of(2026, 10, 28);
        when(portfolioService.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(snapshotService.captureSnapshotForUser(7L, date)).thenReturn(5);

        ResponseEntity<Integer> response = controller.captureSnapshot("anna@example.com", date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody());
        verify(snapshotService).captureSnapshotForUser(7L, date);
    }

    @Test
    void returnsBadRequestWhenRequestedDateHasMissingPrices() {
        PortfolioService portfolioService = mock(PortfolioService.class);
        PortfolioSnapshotService snapshotService = mock(PortfolioSnapshotService.class);
        MarketService marketService = mock(MarketService.class);
        PortfolioSnapshotController controller = new PortfolioSnapshotController(
                portfolioService, snapshotService, marketService);
        User user = new User();
        user.setId(7L);
        LocalDate date = LocalDate.of(2026, 10, 28);
        when(portfolioService.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(snapshotService.captureSnapshotForUser(7L, date))
                .thenThrow(new IllegalStateException("No historical price"));

        ResponseEntity<Integer> response = controller.captureSnapshot("anna@example.com", date);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}