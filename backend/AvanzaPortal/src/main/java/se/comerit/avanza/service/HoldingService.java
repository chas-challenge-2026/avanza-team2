package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import se.comerit.avanza.dto.holdings.CreateHoldingRequestDTO;
import se.comerit.avanza.dto.holdings.HoldingResponseDTO;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.UserRepository;

@Service
public class HoldingService {

    private final JdbcTemplate jdbcTemplate;

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    public HoldingService(JdbcTemplate jdbcTemplate, UserRepository userRepository, AccountRepository accountRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public List<Map<String, Object>> getHoldingsForUser(Integer userId) {
        String holdingSql = "SELECT h.id, h.ticker, h.instrument_name, " +
                "h.quantity, h.avg_buy_price, h.currency, a.account_type, a.account_name " +
                "FROM holdings h " +
                "JOIN accounts a ON h.account_id = a.id " +
                "WHERE a.user_id = ? " +
                "ORDER BY a.account_type, h.ticker";
        return jdbcTemplate.queryForList(holdingSql, userId);
    }

    public List<Map<String, Object>> getAccountsForUser(Integer userId) {
        String accountSql = "SELECT id, account_type, account_name " +
                "FROM accounts WHERE user_id = ?";
        return jdbcTemplate.queryForList(accountSql, userId);
    }

    public List<Map<String, Object>> getEnrichedHoldingsForUser(Integer userId) {
        List<Map<String, Object>> holdings = getHoldingsForUser(userId);

        Map<String, Double> prices = new HashMap<>();
        prices.put("ERIC-B", 74.20);
        prices.put("VOLV-B", 268.50);
        prices.put("AAPL", 187.32);
        prices.put("SWED-A", 193.10);
        prices.put("SAND", 212.80);

        for (Map<String, Object> holding : holdings) {
            String ticker = (String) holding.get("ticker");
            double currentPrice = prices.getOrDefault(ticker, 0.0);
            double quantity = ((BigDecimal) holding.get("quantity")).doubleValue();
            double averageBuyPrice = ((BigDecimal) holding.get("avg_buy_price")).doubleValue();
            double marketValue = quantity * currentPrice;
            double costBasis = quantity * averageBuyPrice;

            holding.put("currentPrice", currentPrice);
            holding.put("marketValue", roundToTwoDecimals(marketValue));
            holding.put("pnl", roundToTwoDecimals(marketValue - costBasis));
        }

        return holdings;
    }



    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public void addHolding(Integer accountId, String ticker, String instrumentName, String quantity, String avgBuyPrice, String currency) {
        String sql = "INSERT INTO holdings (account_id, ticker, instrument_name, quantity, avg_buy_price, currency) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, accountId, ticker.toUpperCase(), instrumentName, new BigDecimal(quantity), new BigDecimal(avgBuyPrice), currency);
    }

    public void deleteHolding(Integer holdingId) {
        String sql = "DELETE FROM holdings WHERE id = ?";
        jdbcTemplate.update(sql, holdingId);
    }

    // This method retrieves the holdings and accounts for the authenticated user based on their email.
    public HoldingResponseDTO getHoldingsForAuthenicatedUser (String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Autentication failed: User not found"));
        
        // Convert the user ID from Long to Integer for compatibility with the rest of the code.
        // Math is only used temporarily to avoid potential overflow issues when converting from Long to Integer.
        Integer userId = Math.toIntExact(user.getId());

        return new HoldingResponseDTO(
            user.getName(),
            getEnrichedHoldingsForUser(userId),
            getAccountsForUser(userId)
        );
    }

    public void addHoldingForAuthenticatedUser(String email, CreateHoldingRequestDTO requestDTO) {
            
            User user = userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Autentication failed: User not found"));
            
    
            // Check if the account belongs to the authenticated user
            boolean ownsAccount = accountRepository.existsByUserIdAndAccountType(requestDTO.accountId().longValue(), user.getId());
            if (!ownsAccount) {
                throw new IllegalArgumentException("Account does not belong to the authenticated user");
            }
    
            addHolding(
                requestDTO.accountId(),
                requestDTO.ticker(),
                requestDTO.instrumentName(),
                requestDTO.quantity(),
                requestDTO.avgBuyPrice(),
                requestDTO.currency()
            );
    }

}
