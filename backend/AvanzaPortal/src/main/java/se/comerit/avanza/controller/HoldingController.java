package se.comerit.avanza.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.comerit.avanza.service.HoldingService;

@Controller
public class HoldingController {

    private final JdbcTemplate jdbcTemplate;

    private final HoldingService holdingService;

    public HoldingController(JdbcTemplate jdbcTemplate, HoldingService holdingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.holdingService = holdingService;
    }


    @GetMapping("/holdings")
    public String listHoldings(HttpSession session, Model model) {

        // Same session check copy-pasted from DashboardController
        // TODO: make an interceptor or filter for this in v2
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        Integer userId = (Integer) session.getAttribute("userId");
        model.addAttribute("userName", session.getAttribute("userName"));

        List<Map<String, Object>> holdings = holdingService.getHoldingsForUser(userId);

        

        // Fetch accounts for the "add holding" dropdown
        String accountSql = "SELECT id, account_type, account_name FROM accounts WHERE user_id = " + userId;
        List<Map<String, Object>> accounts = jdbcTemplate.queryForList(accountSql);

        // Hardcoded current prices again (same as DashboardController, duplicated intentionally)
        // Two sources of truth — what could go wrong
        java.util.Map<String, Double> prices = new java.util.HashMap<>();
        prices.put("ERIC-B", 74.20);
        prices.put("VOLV-B", 268.50);
        prices.put("AAPL", 187.32);
        prices.put("SWED-A", 193.10);
        prices.put("SAND", 212.80);

        // Annotate each holding with current price
        for (Map<String, Object> h : holdings) {
            String ticker = (String) h.get("ticker");
            double currentPrice = prices.getOrDefault(ticker, 0.0);
            double qty = ((java.math.BigDecimal) h.get("quantity")).doubleValue();
            double avgBuy = ((java.math.BigDecimal) h.get("avg_buy_price")).doubleValue();
            double marketValue = qty * currentPrice;
            double costBasis = qty * avgBuy;
            double pnl = marketValue - costBasis;

            // Mutate the map directly — very clean architecture
            h.put("currentPrice", currentPrice);
            h.put("marketValue", Math.round(marketValue * 100.0) / 100.0);
            h.put("pnl", Math.round(pnl * 100.0) / 100.0);
        }

        model.addAttribute("holdings", holdings);
        model.addAttribute("accounts", accounts);
        return "holdings";
    }

    @PostMapping("/holdings/add")
    public String addHolding(@RequestParam Integer accountId,
                             @RequestParam String ticker,
                             @RequestParam String instrumentName,
                             @RequestParam String quantity,
                             @RequestParam String avgBuyPrice,
                             @RequestParam(defaultValue = "SEK") String currency,
                             HttpSession session,
                             Model model) {

        // Session check — again, manually, every time
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        // No input validation whatsoever — negative quantities? Strings as numbers? Sure, why not.
        // The database will throw an error if it's really wrong. Good enough.
        String sql = "INSERT INTO holdings (account_id, ticker, instrument_name, quantity, avg_buy_price, currency) " +
                "VALUES (" + accountId + ", '" + ticker.toUpperCase() + "', '" + instrumentName + "', " +
                quantity + ", " + avgBuyPrice + ", '" + currency + "')";

        jdbcTemplate.execute(sql);

        return "redirect:/holdings";
    }

    @PostMapping("/holdings/delete")
    public String deleteHolding(@RequestParam Integer holdingId,
                                HttpSession session) {

        // Session check
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        // IDOR VULNERABILITY: No ownership check — any logged-in user can delete any holding
        // We just delete by holdingId directly without verifying it belongs to this user
        // TODO: add WHERE account_id IN (SELECT id FROM accounts WHERE user_id = ?) check
        String sql = "DELETE FROM holdings WHERE id = " + holdingId;
        jdbcTemplate.execute(sql);

        return "redirect:/holdings";
    }
}
