package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.entity.PositionSnapshot;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.repository.HistoricalRepository;
import se.comerit.avanza.repository.PositionSnapshotRepository;
import se.comerit.avanza.repository.TargetRepository;

class PortfolioSnapshotServiceTest {

    @Test
    void capturesPositionUsingHistoricalPriceAndFxRate() {
        HoldingService holdingService = mock(HoldingService.class);
        MarketService marketService = mock(MarketService.class);
        PositionSnapshotRepository snapshotRepository = mock(PositionSnapshotRepository.class);
        HistoricalRepository historicalRepository = mock(HistoricalRepository.class);
        TargetRepository targetRepository = mock(TargetRepository.class);

        LocalDate snapshotDate = LocalDate.of(2026, 10, 26);
        when(snapshotRepository.existsByUserIdAndPriceDate(1L, snapshotDate)).thenReturn(false);
        when(holdingService.getHoldingsForUser(1)).thenReturn(List.of(Map.of(
                "account_id", 7L,
                "account_type", "ISK",
                "ticker", "AAPL",
                "currency", "USD",
                "quantity", new BigDecimal("3.000000"))));
        when(historicalRepository.findByTickerInAndPriceDateBetween(
                List.of("AAPL"), snapshotDate, snapshotDate))
                .thenReturn(List.of(new HistoricalStockPrice(
                        "AAPL", snapshotDate, new BigDecimal("100.000000"))));
        when(targetRepository.findByUser_Id(1L))
                .thenReturn(List.of(new TargetAllocations("ISK", 60.0, null)));
        when(marketService.getHistoricalFxRate("USD", "SEK", snapshotDate)).thenReturn(10.0);

        List<PositionSnapshot> savedSnapshots = new ArrayList<>();
        doAnswer(invocation -> {
            Iterable<PositionSnapshot> snapshots = invocation.getArgument(0);
            snapshots.forEach(savedSnapshots::add);
            return savedSnapshots;
        }).when(snapshotRepository).saveAll(any());

        PortfolioSnapshotService service = new PortfolioSnapshotService(
                holdingService,
                marketService,
                snapshotRepository,
                historicalRepository,
                targetRepository);

        int savedCount = service.captureSnapshotForUser(1L, snapshotDate);

        assertEquals(1, savedCount);
        assertEquals(1, savedSnapshots.size());
        PositionSnapshot snapshot = savedSnapshots.get(0);
        assertEquals(1L, snapshot.getUserId());
        assertEquals(7L, snapshot.getAccountId());
        assertEquals("AAPL", snapshot.getTicker());
        assertEquals(snapshotDate, snapshot.getPriceDate());
        assertEquals(0, new BigDecimal("3000.000000").compareTo(snapshot.getSekValue()));
        assertEquals(new BigDecimal("60.0"), snapshot.getTargetAllocation());
        verify(marketService).getHistoricalFxRate("USD", "SEK", snapshotDate);
    }

    @Test
    void skipsSnapshotWhenUserAndDateHaveAlreadyBeenCaptured() {
        HoldingService holdingService = mock(HoldingService.class);
        MarketService marketService = mock(MarketService.class);
        PositionSnapshotRepository snapshotRepository = mock(PositionSnapshotRepository.class);
        HistoricalRepository historicalRepository = mock(HistoricalRepository.class);
        TargetRepository targetRepository = mock(TargetRepository.class);
        LocalDate snapshotDate = LocalDate.of(2026, 10, 26);
        when(snapshotRepository.existsByUserIdAndPriceDate(1L, snapshotDate)).thenReturn(true);

        PortfolioSnapshotService service = new PortfolioSnapshotService(
                holdingService,
                marketService,
                snapshotRepository,
                historicalRepository,
                targetRepository);

        assertEquals(0, service.captureSnapshotForUser(1L, snapshotDate));
        verifyNoInteractions(holdingService, marketService, historicalRepository, targetRepository);
    }
}