package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HoldingService {

    private final JdbcTemplate jdbcTemplate;

    public HoldingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}
