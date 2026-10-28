package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.comerit.avanza.dto.portfolio.PositionSnapshotDTO;
import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.entity.PositionSnapshot;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.repository.HistoricalRepository;
import se.comerit.avanza.repository.PositionSnapshotRepository;
import se.comerit.avanza.repository.TargetRepository;

@Service
public class PortfolioSnapshotService {
    private final HoldingService holdingService;
    private final MarketService marketService;
    private final PositionSnapshotRepository snapshotRepo;
    private final HistoricalRepository historicalRepository;
    private final TargetRepository targetRepository;

    public PortfolioSnapshotService(HoldingService holdingService, MarketService marketService,
            PositionSnapshotRepository snapshotRepo, HistoricalRepository historicalRepository,
            TargetRepository targetRepository) {
        this.holdingService = holdingService;
        this.marketService = marketService;
        this.snapshotRepo = snapshotRepo;
        this.historicalRepository = historicalRepository;
        this.targetRepository = targetRepository;
    }

    /**
     * Captures a snapshot of the user's positions for the given date.
     * 
     * @param userId the user ID
     * @param date   the snapshot date
     * @return number of snapshots saved
     */
    @Transactional
    public int captureSnapshotForUser(Long userId, LocalDate date) {
        if (snapshotRepo.existsByUserIdAndPriceDate(userId, date)) {
            return 0;
        }

        List<PositionSnapshotDTO> holdings = holdingService.getPositionSnapshotInputsForUser(userId);
        if (holdings.isEmpty()) {
            return 0;
        }

        Map<PositionKey, BigDecimal> quantitiesByPosition = new LinkedHashMap<>();
        for (PositionSnapshotDTO holding : holdings) {
            PositionKey key = new PositionKey(
                    holding.accountId(),
                    holding.ticker(),
                    holding.currency(),
                    holding.accountType());
            quantitiesByPosition.merge(key, holding.quantity(), BigDecimal::add);
        }

        List<String> tickers = quantitiesByPosition.keySet().stream()
                .map(PositionKey::ticker)
                .distinct()
                .toList();
        Map<String, HistoricalStockPrice> pricesByTicker = historicalRepository
                .findByTickerInAndPriceDateBetween(tickers, date, date)
                .stream()
                .collect(Collectors.toMap(HistoricalStockPrice::getTicker, price -> price));

        Map<String, BigDecimal> targetsByAccountType = new HashMap<>();
        for (TargetAllocations target : targetRepository.findByUser_Id(userId)) {
            if (target.getTarget_pct() != null) {
                targetsByAccountType.put(
                        target.getAccount_type(),
                        BigDecimal.valueOf(target.getTarget_pct()));
            }
        }

        List<PositionSnapshot> snapshots = new ArrayList<>(quantitiesByPosition.size());
        for (Map.Entry<PositionKey, BigDecimal> entry : quantitiesByPosition.entrySet()) {
            PositionKey position = entry.getKey();
            HistoricalStockPrice historicalPrice = pricesByTicker.get(position.ticker());
            if (historicalPrice == null) {
                throw new IllegalStateException(
                        "No historical price for " + position.ticker() + " on " + date);
            }

            BigDecimal currencyFx = "SEK".equalsIgnoreCase(position.currency())
                    ? BigDecimal.ONE
                    : BigDecimal.valueOf(marketService.getHistoricalFxRate(position.currency(), "SEK", date));
            BigDecimal closePrice = historicalPrice.getClosePrice();
            BigDecimal sekValue = entry.getValue()
                    .multiply(closePrice)
                    .multiply(currencyFx)
                    .setScale(6, RoundingMode.HALF_UP);

            snapshots.add(new PositionSnapshot(
                    userId,
                    position.accountId(),
                    position.ticker(),
                    position.currency(),
                    closePrice,
                    entry.getValue(),
                    date,
                    sekValue,
                    currencyFx,
                    targetsByAccountType.get(position.accountType())));
        }

        return snapshotRepo.saveAll(snapshots).size();
    }

    /**
     * Represents a unique position.
     * 
     * @param accountId   the account ID
     * @param ticker      the stock ticker
     * @param currency    the currency
     * @param accountType the account type
     */
    private record PositionKey(Long accountId, String ticker, String currency, String accountType) {
    }
}