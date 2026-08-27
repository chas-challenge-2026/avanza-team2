package se.comerit.avanza.service;

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
        return jdbcTemplate.queryForList(holdingSql);
    }


    
}
