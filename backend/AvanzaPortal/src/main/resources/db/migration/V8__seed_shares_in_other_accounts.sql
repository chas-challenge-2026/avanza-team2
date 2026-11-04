-- Demo data: give Anna's KF and Depå accounts a few shares of the same stocks as her ISK,
-- so every account has both Aktier and Fonder to rebalance.
INSERT INTO holdings (account_id, ticker, instrument_name, quantity, avg_buy_price, currency, category)
SELECT a.id, v.ticker, v.instrument_name, v.quantity, v.avg_buy_price, v.currency, v.category
FROM accounts a
JOIN users u ON u.id = a.user_id AND u.email = 'anna@example.com'
JOIN (VALUES
    ('ERIC-B', 'Ericsson B', 40, 70.00, 'SEK', 'Aktier'),
    ('VOLV-B', 'Volvo B',    10, 250.00, 'SEK', 'Fonder'),
    ('AAPL',   'Apple Inc',   5, 170.00, 'USD', 'Aktier')
) AS v(ticker, instrument_name, quantity, avg_buy_price, currency, category) ON TRUE
WHERE a.account_type IN ('KF', 'Depa')
  AND NOT EXISTS (
      SELECT 1 FROM holdings h WHERE h.account_id = a.id AND h.ticker = v.ticker
  );
