package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import se.comerit.avanza.dto.holdings.CreateHoldingRequestDTO;
import se.comerit.avanza.dto.holdings.HoldingAccountDTO;
import se.comerit.avanza.dto.holdings.HoldingItemDTO;
import se.comerit.avanza.dto.holdings.HoldingResponseDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.UserRepository;

/**
 * Service class for managing user holdings and accounts.
 * Provides methods to retrieve holdings, enriched holdings with market data,
 * and accounts for a given user.
 */
@Service
public class HoldingService {

    private final UserRepository userRepository;
    private final MarketService marketService;

    private final AccountRepository accountRepository;
    private final HoldingsRepository holdingsRepository;

    public HoldingService(UserRepository userRepository, AccountRepository accountRepository,
            HoldingsRepository holdingsRepository, MarketService marketService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.holdingsRepository = holdingsRepository;
        this.marketService = marketService;
    }

    /**
     * Retrieves the holdings for a given user ID.
     *
     * @param userId the ID of the user
     * @return a list of holdings represented as maps
     */
    public List<Map<String, Object>> getHoldingsForUser(Integer userId) {
        return holdingsRepository.findHoldingsForUser(userId.longValue())
                .stream()
                .map(holding -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", holding.getId());

                    row.put("ticker", holding.getTicker());
                    row.put("instrument_name", holding.getInstrument_name());
                    row.put("quantity", holding.getQuantity());
                    row.put("avg_buy_price", holding.getAvg_buy_price());
                    row.put("currency", holding.getCurrency());

                    row.put("account_id", holding.getAccount().getId());
                    row.put("account_type", holding.getAccount().getAccount_type());
                    row.put("account_name", holding.getAccount().getAccount_name());

                    return row;
                })
                .toList();
    }

    /**
     * Retrieves the accounts for a given user ID.
     *
     * @param userId the ID of the user
     * @return a list of holding account DTOs
     */
    public List<HoldingAccountDTO> getAccountsForUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(account -> new HoldingAccountDTO(
                        account.getId(),
                        account.getAccount_type(),
                        account.getAccount_name()))
                .toList();
    }

    /**
     * Retrieves the enriched holdings for a given user ID, including current market
     * price,
     * market value in SEK, and profit and loss (PnL).
     *
     * @param userId the ID of the user
     * @return a list of enriched holdings represented as maps
     */
    public List<Map<String, Object>> getEnrichedHoldingsForUser(Integer userId) {
        List<Map<String, Object>> holdings = getHoldingsForUser(userId);

        for (Map<String, Object> holding : holdings) {
            String ticker = (String) holding.get("ticker");

            var price = marketService.getPrice(ticker);

            if (price.isEmpty()) {
                holding.put("currentPrice", null);
                holding.put("marketValue", null);
                holding.put("pnl", null);
                continue;
            }

            double currentPrice = price.get().doubleValue();
            double quantity = ((BigDecimal) holding.get("quantity")).doubleValue();
            double averageBuyPrice = ((BigDecimal) holding.get("avg_buy_price")).doubleValue();
            double conversionRate = getSekConversionRate((String) holding.get("currency"));
            double marketValue = toSek(quantity * currentPrice, conversionRate);
            double costBasis = toSek(quantity * averageBuyPrice, conversionRate);

            holding.put("currentPrice", currentPrice);
            holding.put("marketValue", roundToTwoDecimals(marketValue));
            holding.put("pnl", roundToTwoDecimals(marketValue - costBasis));
        }

        return holdings;
    }

    /**
     * Rounds a given value to two decimal places.
     *
     * @param value the value to round
     * @return the rounded value
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Retrieves the conversion rate from the given currency to SEK.
     *
     * @param currency the currency code (e.g., "USD")
     * @return the conversion rate to SEK
     */
    private double getSekConversionRate(String currency) {
        return "USD".equalsIgnoreCase(currency)
                ? marketService.getFx("USD", "SEK").rate()
                : 1.0;
    }

    /**
     * Converts the given amount to SEK using the provided conversion rate.
     *
     * @param amount         the amount in the original currency
     * @param conversionRate the conversion rate to SEK
     * @return the amount converted to SEK
     */
    private double toSek(double amount, double conversionRate) {
        return amount * conversionRate;
    }

    /**
     * Adds a new holding for the specified account.
     *
     * @param accountId      the ID of the account
     * @param ticker         the ticker symbol of the holding
     * @param instrumentName the name of the instrument
     * @param quantity       the quantity of the holding
     * @param avgBuyPrice    the average buy price of the holding
     * @param currency       the currency of the holding
     */
    private void addHolding(Integer accountId, String ticker, String instrumentName, String quantity,
            String avgBuyPrice, String currency) {

        BigDecimal parsedQuantity = new BigDecimal(quantity);
        BigDecimal parsedAvgBuyPrice = new BigDecimal(avgBuyPrice);

        Account account = accountRepository.findById(accountId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Holdings holding = new Holdings(ticker.toUpperCase(), instrumentName, parsedQuantity, parsedAvgBuyPrice,
                currency, account);

        holdingsRepository.save(holding);

    }

    /**
     * Retrieves the holdings and accounts for the authenticated user based on their
     * email.
     *
     * @param email    the email of the authenticated user
     * @param pageable the pagination information
     * @return a HoldingResponseDTO containing the user's holdings
     */
    public HoldingResponseDTO getHoldingsForAuthenticatedUser(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Autentication failed: User not found"));

        Page<Holdings> holdingsPage = holdingsRepository.findAllByUserId(user.getId(), pageable);
        List<Map<String, Object>> pageHoldings = holdingsPage.getContent().stream()
                .map(holding -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", holding.getId());
                    row.put("ticker", holding.getTicker());
                    row.put("instrument_name", holding.getInstrument_name());
                    row.put("quantity", holding.getQuantity());
                    row.put("avg_buy_price", holding.getAvg_buy_price());
                    row.put("currency", holding.getCurrency());
                    row.put("account_type", holding.getAccount().getAccount_type());
                    row.put("account_name", holding.getAccount().getAccount_name());
                    return row;
                })
                .toList();

        for (Map<String, Object> holding : pageHoldings) {
            String ticker = (String) holding.get("ticker");
            double currentPrice = marketService.getPrice(ticker)
                    .map(BigDecimal::doubleValue)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve current price for ticker: " + ticker));
            double quantity = ((BigDecimal) holding.get("quantity")).doubleValue();
            double averageBuyPrice = ((BigDecimal) holding.get("avg_buy_price")).doubleValue();
            double conversionRate = getSekConversionRate((String) holding.get("currency"));
            double marketValue = toSek(quantity * currentPrice, conversionRate);
            double costBasis = toSek(quantity * averageBuyPrice, conversionRate);

            holding.put("currentPrice", currentPrice);
            holding.put("marketValue", roundToTwoDecimals(marketValue));
            holding.put("pnl", roundToTwoDecimals(marketValue - costBasis));
        }

        List<HoldingItemDTO> holdings = pageHoldings.stream().map(this::toHoldingDTO).toList();

        return new HoldingResponseDTO(
                user.getName(),
                holdings,
                getAccountsForUser(user.getId()),
                holdingsPage.getNumber(),
                holdingsPage.getSize(),
                holdingsPage.getTotalElements(),
                holdingsPage.getTotalPages());
    }

    /**
     * Adds a new holding for the authenticated user.
     *
     * @param email      the email of the authenticated user
     * @param requestDTO the request DTO containing holding details
     */
    public void addHoldingForAuthenticatedUser(String email, CreateHoldingRequestDTO requestDTO) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Autentication failed: User not found"));

        // Check if the account belongs to the authenticated user
        boolean ownsAccount = accountRepository.existsByIdAndUser_Id(requestDTO.accountId().longValue(), user.getId());
        if (!ownsAccount) {
            throw new AccessDeniedException("You do not have permission to add a holding to this account.");
        }

        addHolding(
                requestDTO.accountId(),
                requestDTO.ticker(),
                requestDTO.instrumentName(),
                requestDTO.quantity(),
                requestDTO.avgBuyPrice(),
                requestDTO.currency());
    }

    /**
     * Deletes a holding for the authenticated user.
     *
     * @param email     the email of the authenticated user
     * @param holdingId the ID of the holding to delete
     */
    public void deleteHoldingForAuthenticatedUser(String email, Integer holdingId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Autentication failed: User not found"));

        // A single ownership-scoped delete prevents deleting another user's holding.
        int deletedRows = holdingsRepository.deleteOwnedHolding(holdingId.longValue(), user.getId());
        if (deletedRows == 0) {
            throw new AccessDeniedException("You do not have permission to delete this holding.");
        }
    }

    /**
     * Converts a database row into a HoldingItemDTO.
     *
     * @param row the database row as a map
     * @return the corresponding HoldingItemDTO
     */
    private HoldingItemDTO toHoldingDTO(Map<String, Object> row) {
        return new HoldingItemDTO(
                toLong(row.get("id")),
                (String) row.get("ticker"),
                (String) row.get("instrument_name"),
                (BigDecimal) row.get("quantity"),
                (BigDecimal) row.get("avg_buy_price"),
                (String) row.get("currency"),
                (String) row.get("account_type"),
                (String) row.get("account_name"),
                toDouble(row.get("currentPrice")),
                toDouble(row.get("marketValue")),
                toDouble(row.get("pnl")));
    }

    /**
     * Converts an object to a Double.
     *
     * @param value the object to convert
     * @return the corresponding Double value, or null if the input is null
     */
    private Double toDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    /**
     * Converts an object to a Long.
     *
     * @param value the object to convert
     * @return the corresponding Long value, or null if the input is null
     */
    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

}
