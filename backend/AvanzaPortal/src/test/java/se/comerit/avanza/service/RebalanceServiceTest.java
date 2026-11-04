package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.BadCredentialsException;

import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.dto.rebalance.RebalancePreviewDTO;
import se.comerit.avanza.dto.rebalance.RebalanceTradeDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.exception.InvalidRequestException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RebalanceServiceTest {

    private static final String EMAIL = "anna@example.com";
    private static final Long USER_ID = 1L;
    private static final Long ACCOUNT_ID = 5L;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingsRepository holdingsRepository;

    @Mock
    private MarketService marketService;

    @Mock
    private UserRepository userRepository;

    private RebalanceService rebalanceService;
    private Account account;

    @BeforeEach
    void setUp() {
        rebalanceService = new RebalanceService(accountRepository, holdingsRepository, marketService,
                userRepository);

        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        account = new Account();
        account.setId(ACCOUNT_ID);
        account.setUser(user);

        lenient().when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        lenient().when(accountRepository.findByIdAndUser_Id(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
    }

    private Holdings holding(long id, String ticker, String category, String quantity, String avgBuy,
            String currency) {
        Holdings holding = new Holdings(ticker, ticker, new BigDecimal(quantity), new BigDecimal(avgBuy), currency,
                account);
        holding.setId(id);
        holding.setCategory(category);
        return holding;
    }

    private void givenHoldings(Holdings... holdings) {
        when(holdingsRepository.findAllByAccountIdIn(List.of(ACCOUNT_ID), Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(holdings)));
    }

    private void givenPrice(String ticker, String price) {
        when(marketService.getPrice(ticker)).thenReturn(Optional.of(new BigDecimal(price)));
    }

    // 600 SEK Aktier (AAA) and 400 SEK Fonder (BBB).
    private void givenSixtyFortyAccount() {
        givenHoldings(
                holding(1L, "AAA", "Aktier", "10", "50", "SEK"),
                holding(2L, "BBB", "Fonder", "10", "30", "SEK"));
        givenPrice("AAA", "60");
        givenPrice("BBB", "40");
    }

    @Test
    void shouldPreviewSellingAktierAndBuyingFonderWhenAboveTarget() {
        givenSixtyFortyAccount();

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("50"));

        assertEquals(0, new BigDecimal("1000.00").compareTo(preview.totalValue()));
        assertEquals(0, new BigDecimal("60.00").compareTo(preview.currentAktierPct()));
        assertEquals(0, new BigDecimal("40.00").compareTo(preview.currentFonderPct()));
        assertEquals(0, new BigDecimal("50.00").compareTo(preview.targetAktierPct()));
        assertEquals(0, new BigDecimal("50.00").compareTo(preview.targetFonderPct()));
        assertTrue(preview.drifted());
        assertEquals(2, preview.trades().size());

        RebalanceTradeDTO sell = preview.trades().get(0);
        assertEquals("SELL", sell.side());
        assertEquals("AAA", sell.ticker());
        assertEquals(0, new BigDecimal("1.6666").compareTo(sell.quantity()));

        RebalanceTradeDTO buy = preview.trades().get(1);
        assertEquals("BUY", buy.side());
        assertEquals("BBB", buy.ticker());
        assertEquals(0, new BigDecimal("2.5").compareTo(buy.quantity()));
    }

    @Test
    void shouldPreviewWithoutChangingHoldings() {
        givenSixtyFortyAccount();

        rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("50"));

        verify(holdingsRepository, never()).save(any());
        verify(holdingsRepository, never()).delete(any());
    }

    @Test
    void shouldFlagDriftWhenCurrentSplitIsMoreThanFivePointsFromTarget() {
        givenSixtyFortyAccount();

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("40"));

        assertTrue(preview.drifted());
    }

    @Test
    void shouldNotFlagDriftAtExactlyFivePoints() {
        givenSixtyFortyAccount();

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("55"));

        assertFalse(preview.drifted());
    }

    @Test
    void shouldPreviewBuyingAktierAndSellingFonderWhenBelowTarget() {
        givenSixtyFortyAccount();

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("70"));

        assertEquals("SELL", preview.trades().get(0).side());
        assertEquals("BBB", preview.trades().get(0).ticker());
        assertEquals("BUY", preview.trades().get(1).side());
        assertEquals("AAA", preview.trades().get(1).ticker());
    }

    @Test
    void shouldReturnNoTradesWhenAlreadyAtTarget() {
        givenSixtyFortyAccount();

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("60"));

        assertTrue(preview.trades().isEmpty());
        assertEquals(null, preview.warning());
    }

    @Test
    void shouldWarnWhenAccountHasNoHoldings() {
        givenHoldings();

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("50"));

        assertTrue(preview.trades().isEmpty());
        assertNotNull(preview.warning());
        assertFalse(preview.drifted());
        assertEquals(0, BigDecimal.ZERO.compareTo(preview.totalValue()));
    }

    @Test
    void shouldWarnWhenThereIsNothingToBuyOnTheTargetSide() {
        givenHoldings(holding(1L, "AAA", "Aktier", "10", "50", "SEK"));
        givenPrice("AAA", "60");

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("50"));

        assertTrue(preview.trades().isEmpty());
        assertNotNull(preview.warning());
    }

    @Test
    void shouldFallBackToAverageBuyPriceWhenNoLivePrice() {
        givenHoldings(
                holding(1L, "AAA", "Aktier", "10", "60", "SEK"),
                holding(2L, "BBB", "Fonder", "10", "40", "SEK"));
        when(marketService.getPrice("AAA")).thenReturn(Optional.empty());
        when(marketService.getPrice("BBB")).thenReturn(Optional.empty());

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("60"));

        assertEquals(0, new BigDecimal("1000.00").compareTo(preview.totalValue()));
        assertEquals(0, new BigDecimal("60.00").compareTo(preview.currentAktierPct()));
    }

    @Test
    void shouldConvertForeignCurrencyHoldingsToSek() {
        givenHoldings(
                holding(1L, "AAPL", "Aktier", "10", "5", "USD"),
                holding(2L, "BBB", "Fonder", "10", "40", "SEK"));
        givenPrice("AAPL", "10");
        givenPrice("BBB", "40");
        when(marketService.getFx("USD", "SEK")).thenReturn(new FxRateResponseDTO("2026-11-03", "USD", "SEK", 10.0));

        RebalancePreviewDTO preview = rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("50"));

        // 10 * 10 * 10 = 1000 SEK Aktier + 400 SEK Fonder.
        assertEquals(0, new BigDecimal("1400.00").compareTo(preview.totalValue()));
    }

    @Test
    void shouldRejectTargetOutsideZeroToHundred() {
        assertThrows(InvalidRequestException.class,
                () -> rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("100.01")));
        assertThrows(InvalidRequestException.class,
                () -> rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("-1")));
        assertThrows(InvalidRequestException.class,
                () -> rebalanceService.previewRebalance(EMAIL, ACCOUNT_ID, null));
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> rebalanceService.previewRebalance("nobody@example.com", ACCOUNT_ID, new BigDecimal("50")));
    }

    @Test
    void shouldRejectAccountNotOwnedByUser() {
        when(accountRepository.findByIdAndUser_Id(99L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rebalanceService.previewRebalance(EMAIL, 99L, new BigDecimal("50")));
        assertThrows(ResourceNotFoundException.class,
                () -> rebalanceService.applyRebalance(EMAIL, 99L, new BigDecimal("50")));
        verify(holdingsRepository, never()).save(any());
    }

    @Test
    void shouldApplyRebalanceByAdjustingQuantities() {
        Holdings stock = holding(1L, "AAA", "Aktier", "10", "50", "SEK");
        Holdings fund = holding(2L, "BBB", "Fonder", "10", "30", "SEK");
        givenHoldings(stock, fund);
        givenPrice("AAA", "60");
        givenPrice("BBB", "40");

        RebalancePreviewDTO result = rebalanceService.applyRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("50"));

        assertEquals(2, result.trades().size());
        assertEquals(0, new BigDecimal("8.3334").compareTo(stock.getQuantity()));
        assertEquals(0, new BigDecimal("12.5").compareTo(fund.getQuantity()));
        // (10 * 30 + 2.5 * 40) / 12.5 = 32
        assertEquals(0, new BigDecimal("32").compareTo(fund.getAvg_buy_price()));
        verify(holdingsRepository).save(stock);
        verify(holdingsRepository).save(fund);
        verify(holdingsRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteHoldingWhenFullySold() {
        Holdings stock = holding(1L, "AAA", "Aktier", "10", "50", "SEK");
        Holdings fund = holding(2L, "BBB", "Fonder", "10", "30", "SEK");
        givenHoldings(stock, fund);
        givenPrice("AAA", "60");
        givenPrice("BBB", "40");

        rebalanceService.applyRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("100"));

        verify(holdingsRepository).delete(fund);
        verify(holdingsRepository).save(stock);
        assertEquals(0, new BigDecimal("16.6666").compareTo(stock.getQuantity().setScale(4,
                java.math.RoundingMode.DOWN)));
    }

    @Test
    void shouldNotChangeAnythingWhenApplyingAtTarget() {
        givenSixtyFortyAccount();

        RebalancePreviewDTO result = rebalanceService.applyRebalance(EMAIL, ACCOUNT_ID, new BigDecimal("60"));

        assertTrue(result.trades().isEmpty());
        verify(holdingsRepository, never()).save(any());
        verify(holdingsRepository, never()).delete(any());
    }
}
