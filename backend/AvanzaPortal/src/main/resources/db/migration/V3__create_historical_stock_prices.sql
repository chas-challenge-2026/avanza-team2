CREATE TABLE historical_stock_prices (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    price_date DATE NOT NULL,
    close_price NUMERIC(18, 6) NOT NULL CHECK (close_price > 0),
    CONSTRAINT uq_historical_stock_prices_ticker_date UNIQUE (ticker, price_date)
);

CREATE INDEX idx_historical_stock_prices_date_ticker ON historical_stock_prices (price_date, ticker);