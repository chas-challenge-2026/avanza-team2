package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.comerit.avanza.dto.rebalance.RebalancePreviewDTO;
import se.comerit.avanza.dto.rebalance.RebalanceTradeDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.exception.InvalidRequestException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.UserRepository;

/**
 * Per-account rebalancing between the "Aktier" and "Fonder" categories.
 * Nothing happens automatically: the user chooses the target split and
 * explicitly applies it.
 */
@Service
public class RebalanceService {
    private static final String AKTIER = "Aktier";
    private static final String FONDER = "Fonder";
    private static final String BASE_CURRENCY = "SEK";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal DRIFT_THRESHOLD_PCT = BigDecimal.valueOf(5);
    private static final int QTY_SCALE = 4;
    private static final int MONEY_SCALE = 2;

    private final MarketService marketService;
    private final AccountRepository accountRepository;
    private final HoldingsRepository holdingsRepository;
    private final UserRepository userRepository;

    public RebalanceService(AccountRepository accountRepository, HoldingsRepository holdingsRepository,
            MarketService marketService, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.holdingsRepository = holdingsRepository;
        this.marketService = marketService;
        this.userRepository = userRepository;
    }

    /** A holding with its current price and SEK value. */
    private record Position(Holdings holding, BigDecimal price, BigDecimal fx, BigDecimal value) {
        boolean isAktie() {
            return !FONDER.equalsIgnoreCase(holding.getCategory());
        }

        /** SEK value per one unit of the holding. */
        BigDecimal unitValue() {
            return price.multiply(fx);
        }
    }

    /**
     * Calculates the trades needed to reach the target split without changing
     * anything. The result is exactly what {@link #applyRebalance} will do.
     */
    @Transactional(readOnly = true)
    public RebalancePreviewDTO previewRebalance(String email, Long accountId, BigDecimal targetAktierPct) {
        Account account = findOwnedAccount(email, accountId);
        return buildPlan(account, targetAktierPct).preview();
    }

    /**
     * Moves value between Aktier and Fonder so the account matches the target
     * split. Only calculated per account.
     */
    @Transactional
    public RebalancePreviewDTO applyRebalance(String email, Long accountId, BigDecimal targetAktierPct) {
        Account account = findOwnedAccount(email, accountId);
        Plan plan = buildPlan(account, targetAktierPct);

        for (TradeAction action : plan.actions()) {
            Holdings h = action.position().holding();
            BigDecimal qty = action.quantity();
            if (action.sell()) {
                BigDecimal remaining = h.getQuantity().subtract(qty);
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    holdingsRepository.delete(h);
                } else {
                    h.setQuantity(remaining);
                    holdingsRepository.save(h);
                }
            } else {
                BigDecimal oldQty = h.getQuantity();
                BigDecimal newQty = oldQty.add(qty);
                BigDecimal newAvg = oldQty.multiply(h.getAvg_buy_price())
                        .add(qty.multiply(action.position().price()))
                        .divide(newQty, 6, RoundingMode.HALF_UP);
                h.setQuantity(newQty);
                h.setAvg_buy_price(newAvg);
                holdingsRepository.save(h);
            }
        }
        return plan.preview();
    }

    private record TradeAction(Position position, boolean sell, BigDecimal quantity) {
    }

    private record Plan(RebalancePreviewDTO preview, List<TradeAction> actions) {
    }

    private Plan buildPlan(Account account, BigDecimal targetAktierPct) {
        if (targetAktierPct == null || targetAktierPct.signum() < 0 || targetAktierPct.compareTo(HUNDRED) > 0) {
            throw new InvalidRequestException("Target percentage must be between 0 and 100");
        }

        List<Position> positions = loadPositions(account.getId());
        BigDecimal aktierValue = sumValue(positions, true);
        BigDecimal fonderValue = sumValue(positions, false);
        BigDecimal total = aktierValue.add(fonderValue);
        if (total.signum() == 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(MONEY_SCALE);
            BigDecimal target = targetAktierPct.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            return new Plan(new RebalancePreviewDTO(account.getId(), zero, zero, zero, target,
                    HUNDRED.subtract(target), false, List.of(), "Kontot saknar innehav att rebalansera."),
                    List.of());
        }

        BigDecimal currentAktierPct = aktierValue.multiply(HUNDRED).divide(total, MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal targetPct = targetAktierPct.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        boolean drifted = currentAktierPct.subtract(targetPct).abs().compareTo(DRIFT_THRESHOLD_PCT) > 0;

        // Positive diff: Aktier is under target, so sell Fonder and buy Aktier.
        BigDecimal targetAktierValue = total.multiply(targetPct).divide(HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal diff = targetAktierValue.subtract(aktierValue);
        boolean buyAktier = diff.signum() > 0;
        BigDecimal amount = diff.abs();

        List<TradeAction> actions = new ArrayList<>();
        String warning = null;
        if (amount.signum() > 0) {
            List<Position> sellSide = positions.stream().filter(p -> p.isAktie() != buyAktier).toList();
            List<Position> buySide = positions.stream().filter(p -> p.isAktie() == buyAktier).toList();
            BigDecimal sellBucket = buyAktier ? fonderValue : aktierValue;
            BigDecimal buyBucket = buyAktier ? aktierValue : fonderValue;

            if (buyBucket.signum() == 0) {
                warning = "Kontot har inga " + (buyAktier ? AKTIER : FONDER)
                        + "-innehav att köpa mer av. Lägg till ett innehav först.";
            } else {
                addActions(actions, sellSide, sellBucket, amount, true);
                addActions(actions, buySide, buyBucket, amount, false);
            }
        }

        List<RebalanceTradeDTO> trades = actions.stream().map(this::toTradeDTO).toList();
        BigDecimal newAktierPct = targetPct;
        RebalancePreviewDTO preview = new RebalancePreviewDTO(
                account.getId(),
                total.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                currentAktierPct,
                HUNDRED.subtract(currentAktierPct),
                newAktierPct,
                HUNDRED.subtract(newAktierPct),
                drifted,
                trades,
                warning);
        return new Plan(preview, actions);
    }

    /** Spreads amount (SEK) over the side's positions in proportion to their value. */
    private void addActions(List<TradeAction> actions, List<Position> side, BigDecimal bucketValue,
            BigDecimal amount, boolean sell) {
        for (Position p : side) {
            if (p.unitValue().signum() == 0 || bucketValue.signum() == 0) {
                continue;
            }
            BigDecimal share = amount.multiply(p.value()).divide(bucketValue, 10, RoundingMode.HALF_UP);
            BigDecimal qty = share.divide(p.unitValue(), QTY_SCALE, RoundingMode.DOWN);
            if (sell) {
                qty = qty.min(p.holding().getQuantity());
            }
            if (qty.signum() > 0) {
                actions.add(new TradeAction(p, sell, qty));
            }
        }
    }

    private RebalanceTradeDTO toTradeDTO(TradeAction a) {
        Holdings h = a.position().holding();
        return new RebalanceTradeDTO(
                h.getId(),
                h.getTicker(),
                h.getCategory(),
                a.sell() ? "SELL" : "BUY",
                a.quantity(),
                a.position().price(),
                a.quantity().multiply(a.position().unitValue()).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }

    private List<Position> loadPositions(Long accountId) {
        List<Holdings> holdings = holdingsRepository
                .findAllByAccountIdIn(List.of(accountId), Pageable.unpaged()).getContent();
        List<Position> positions = new ArrayList<>();
        for (Holdings h : holdings) {
            // Fall back to the buy price when no live price is available.
            BigDecimal price = marketService.getPrice(h.getTicker()).orElse(h.getAvg_buy_price());
            BigDecimal fx = fxToBase(h.getCurrency());
            BigDecimal value = h.getQuantity().multiply(price).multiply(fx);
            positions.add(new Position(h, price, fx, value));
        }
        return positions;
    }

    private BigDecimal fxToBase(String currency) {
        if (currency == null || BASE_CURRENCY.equalsIgnoreCase(currency)) {
            return BigDecimal.ONE;
        }
        return BigDecimal.valueOf(marketService.getFx(currency.toUpperCase(), BASE_CURRENCY).rate());
    }

    private BigDecimal sumValue(List<Position> positions, boolean aktier) {
        return positions.stream()
                .filter(p -> p.isAktie() == aktier)
                .map(Position::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Account findOwnedAccount(String email, Long accountId) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Authentication failed: User not found"))
                .getId();
        return accountRepository.findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
