package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/**
 * Captures the user's current positions using prices and FX rates for the
 * requested date. Saved snapshots can later be used to build portfolio
 * performance history and calculate a portfolio-level Sharpe ratio.
 */
@Service
public class PortfolioSnapshotService {
    private static final long PRICE_DATE_LOOKBACK_DAYS = 14;

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
        return saveSnapshots(userId, date, holdings);
    }

    @Transactional
    public int captureLatestSnapshotForUser(Long userId, LocalDate throughDate) {
        List<PositionSnapshotDTO> holdings = holdingService.getPositionSnapshotInputsForUser(userId);
        if (holdings.isEmpty()) {
            return 0;
        }

        List<String> tickers = holdings.stream()
                .map(PositionSnapshotDTO::ticker)
                .distinct()
                .toList();
        LocalDate fromDate = throughDate.minusDays(PRICE_DATE_LOOKBACK_DAYS);
        Map<LocalDate, Set<String>> tickersByDate = new HashMap<>();
        for (HistoricalStockPrice price : historicalRepository
                .findByTickerInAndPriceDateBetween(tickers, fromDate, throughDate)) {
            tickersByDate.computeIfAbsent(price.getPriceDate(), ignored -> new HashSet<>())
                    .add(price.getTicker());
        }

        Set<String> requiredTickers = Set.copyOf(tickers);
        LocalDate latestCommonDate = tickersByDate.entrySet().stream()
                .filter(entry -> entry.getValue().containsAll(requiredTickers))
                .map(Map.Entry::getKey)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (latestCommonDate == null || snapshotRepo.existsByUserIdAndPriceDate(userId, latestCommonDate)) {
            return 0;
        }
        return saveSnapshots(userId, latestCommonDate, holdings);
    }

    private int saveSnapshots(Long userId, LocalDate date, List<PositionSnapshotDTO> holdings) {
        if (holdings.isEmpty()) {
            return 0;
        }

        Map<PositionKey, BigDecimal> quantitiesByPosition = groupPositions(holdings);
        Map<String, HistoricalStockPrice> pricesByTicker = loadHistoricalPrices(quantitiesByPosition, date);
        Map<String, BigDecimal> targetsByAccountType = loadTargetsByAccountType(userId);
        List<PositionSnapshot> snapshots = buildSnapshots(
                userId, date, quantitiesByPosition, pricesByTicker, targetsByAccountType);
        return snapshotRepo.saveAll(snapshots).size();
    }

    /**
     * Merge duplicate lots so each account/ticker is stored once per snapshot date.
     * 
     * @param holdings list of positions
     * @return map of positions with summed quantities
     */
    private Map<PositionKey, BigDecimal> groupPositions(List<PositionSnapshotDTO> holdings) {
        Map<PositionKey, BigDecimal> quantitiesByPosition = new LinkedHashMap<>();
        for (PositionSnapshotDTO holding : holdings) {
            PositionKey key = new PositionKey(
                    holding.accountId(),
                    holding.ticker(),
                    holding.currency(),
                    holding.accountType());
            quantitiesByPosition.merge(key, holding.quantity(), BigDecimal::add);
        }
        return quantitiesByPosition;
    }

    /**
     * Fetch all required closes in one repository call for the requested date.
     * 
     * @param positions positions to fetch
     * @param date      snapshot date
     * @return map of tickers to historical prices
     */
    private Map<String, HistoricalStockPrice> loadHistoricalPrices(
            Map<PositionKey, BigDecimal> positions,
            LocalDate date) {
        List<String> tickers = positions.keySet().stream()
                .map(PositionKey::ticker)
                .distinct()
                .toList();
        return historicalRepository.findByTickerInAndPriceDateBetween(tickers, date, date)
                .stream()
                .collect(Collectors.toMap(HistoricalStockPrice::getTicker, price -> price));
    }

    /**
     * Preserve each account type's target percentage on the snapshot rows.
     * 
     * @param userId user id
     * @return map of account type to target pct
     */
    private Map<String, BigDecimal> loadTargetsByAccountType(Long userId) {
        Map<String, BigDecimal> targetsByAccountType = new HashMap<>();
        for (TargetAllocations target : targetRepository.findByUser_Id(userId)) {
            if (target.getTarget_pct() != null) {
                targetsByAccountType.put(
                        target.getAccount_type(),
                        BigDecimal.valueOf(target.getTarget_pct()));
            }
        }
        return targetsByAccountType;
    }

    /**
     * Convert the prepared position data into rows ready to persist.
     * 
     * @param userId               user id
     * @param date                 snapshot date
     * @param quantitiesByPosition quantities per position
     * @param pricesByTicker       historical prices
     * @param targetsByAccountType target pct by account type
     * @return list of snapshots
     */
    private List<PositionSnapshot> buildSnapshots(
            Long userId,
            LocalDate date,
            Map<PositionKey, BigDecimal> quantitiesByPosition,
            Map<String, HistoricalStockPrice> pricesByTicker,
            Map<String, BigDecimal> targetsByAccountType) {
        List<PositionSnapshot> snapshots = new ArrayList<>(quantitiesByPosition.size());
        for (Map.Entry<PositionKey, BigDecimal> entry : quantitiesByPosition.entrySet()) {
            snapshots.add(createSnapshot(
                    userId,
                    date,
                    entry.getKey(),
                    entry.getValue(),
                    pricesByTicker,
                    targetsByAccountType));
        }
        return snapshots;
    }

    /**
     * Calculate one position's SEK value and preserve the market inputs used.
     * 
     * @param userId               user id
     * @param date                 snapshot date
     * @param position             position key
     * @param quantity             position quantity
     * @param pricesByTicker       historical prices
     * @param targetsByAccountType target pct by account type
     * @return snapshot
     */
    private PositionSnapshot createSnapshot(
            Long userId,
            LocalDate date,
            PositionKey position,
            BigDecimal quantity,
            Map<String, HistoricalStockPrice> pricesByTicker,
            Map<String, BigDecimal> targetsByAccountType) {
        HistoricalStockPrice historicalPrice = pricesByTicker.get(position.ticker());
        if (historicalPrice == null) {
            throw new IllegalStateException(
                    "No historical price for " + position.ticker() + " on " + date);
        }

        BigDecimal currencyFx = "SEK".equalsIgnoreCase(position.currency())
                ? BigDecimal.ONE
                : BigDecimal.valueOf(marketService.getHistoricalFxRate(position.currency(), "SEK", date));
        BigDecimal closePrice = historicalPrice.getClosePrice();
        BigDecimal sekValue = quantity
                .multiply(closePrice)
                .multiply(currencyFx)
                .setScale(6, RoundingMode.HALF_UP);

        return new PositionSnapshot(
                userId,
                position.accountId(),
                position.ticker(),
                position.currency(),
                closePrice,
                quantity,
                date,
                sekValue,
                currencyFx,
                targetsByAccountType.get(position.accountType()));
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