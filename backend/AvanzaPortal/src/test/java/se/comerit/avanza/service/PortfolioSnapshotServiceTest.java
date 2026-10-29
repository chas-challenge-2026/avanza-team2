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

import org.junit.jupiter.api.Test;

import se.comerit.avanza.dto.portfolio.PositionSnapshotDTO;
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
                when(holdingService.getPositionSnapshotInputsForUser(1L)).thenReturn(List.of(
                                new PositionSnapshotDTO(
                                                7L, "AAPL", "USD", "ISK", new BigDecimal("3.000000"))));
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

        @Test
        void capturesLatestDateWithPricesForAllCurrentTickers() {
                HoldingService holdingService = mock(HoldingService.class);
                MarketService marketService = mock(MarketService.class);
                PositionSnapshotRepository snapshotRepository = mock(PositionSnapshotRepository.class);
                HistoricalRepository historicalRepository = mock(HistoricalRepository.class);
                TargetRepository targetRepository = mock(TargetRepository.class);

                Long userId = 1L;
                LocalDate throughDate = LocalDate.of(2026, 10, 28);
                LocalDate latestCommonDate = LocalDate.of(2026, 10, 26);
                List<String> tickers = List.of("AAPL", "ERIC-B");
                when(holdingService.getPositionSnapshotInputsForUser(userId)).thenReturn(List.of(
                                new PositionSnapshotDTO(7L, "AAPL", "USD", "ISK", new BigDecimal("3")),
                                new PositionSnapshotDTO(8L, "ERIC-B", "SEK", "KF", new BigDecimal("2"))));
                when(historicalRepository.findByTickerInAndPriceDateBetween(
                                tickers, throughDate.minusDays(14), throughDate))
                                .thenReturn(List.of(
                                                new HistoricalStockPrice("AAPL", LocalDate.of(2026, 10, 27),
                                                                new BigDecimal("101")),
                                                new HistoricalStockPrice("AAPL", latestCommonDate,
                                                                new BigDecimal("100")),
                                                new HistoricalStockPrice("ERIC-B", latestCommonDate,
                                                                new BigDecimal("50"))));
                when(snapshotRepository.existsByUserIdAndPriceDate(userId, latestCommonDate)).thenReturn(false);
                when(historicalRepository.findByTickerInAndPriceDateBetween(tickers, latestCommonDate,
                                latestCommonDate))
                                .thenReturn(List.of(
                                                new HistoricalStockPrice("AAPL", latestCommonDate,
                                                                new BigDecimal("100")),
                                                new HistoricalStockPrice("ERIC-B", latestCommonDate,
                                                                new BigDecimal("50"))));
                when(targetRepository.findByUser_Id(userId)).thenReturn(List.of());
                when(marketService.getHistoricalFxRate("USD", "SEK", latestCommonDate)).thenReturn(10.0);
                when(snapshotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

                PortfolioSnapshotService service = new PortfolioSnapshotService(
                                holdingService,
                                marketService,
                                snapshotRepository,
                                historicalRepository,
                                targetRepository);

                assertEquals(2, service.captureLatestSnapshotForUser(userId, throughDate));
        }
}