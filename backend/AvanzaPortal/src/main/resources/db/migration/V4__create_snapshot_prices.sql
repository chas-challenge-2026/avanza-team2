CREATE TABLE position_snapshot (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    account_id INTEGER NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    ticker VARCHAR(20) NOT NULL,
    price_date DATE NOT NULL,
    quantity NUMERIC(18, 6) NOT NULL CHECK (quantity > 0),
    close_price NUMERIC(18, 6) NOT NULL CHECK (close_price > 0),
    currency VARCHAR(3) NOT NULL,
    currency_fx NUMERIC(18, 8) NOT NULL DEFAULT 1 CHECK (currency_fx > 0),
    sek_value NUMERIC(18, 6) NOT NULL CHECK (sek_value >= 0),
    target_allocation NUMERIC(5, 2) CHECK (
        target_allocation BETWEEN 0 AND 100
    ),
    CONSTRAINT uq_snapshot_prices_user_account_ticker_date UNIQUE (
        user_id,
        account_id,
        ticker,
        price_date
    )
);

CREATE INDEX idx_snapshot_prices_user_date ON position_snapshot (user_id, price_date);