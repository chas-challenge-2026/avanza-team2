WITH
    recent_weekdays AS (
        SELECT day::date AS price_date
        FROM generate_series(
                CURRENT_DATE - 14, CURRENT_DATE - 1, interval '1 day'
            ) AS day
        WHERE
            EXTRACT(
                ISODOW
                FROM day
            ) BETWEEN 1 AND 5
        ORDER BY day DESC
        LIMIT 3
    ),
    demo_dates AS (
        SELECT price_date, ROW_NUMBER() OVER (
                ORDER BY price_date
            ) AS day_number
        FROM recent_weekdays
    ),
    demo_prices (
        ticker,
        day_number,
        close_price,
        currency_fx
    ) AS (
        VALUES (
                'ERIC-B',
                1,
                69.50::numeric,
                1.00000000::numeric
            ),
            (
                'ERIC-B',
                2,
                70.80::numeric,
                1.00000000::numeric
            ),
            (
                'ERIC-B',
                3,
                70.10::numeric,
                1.00000000::numeric
            ),
            (
                'VOLV-B',
                1,
                257.00::numeric,
                1.00000000::numeric
            ),
            (
                'VOLV-B',
                2,
                261.50::numeric,
                1.00000000::numeric
            ),
            (
                'VOLV-B',
                3,
                259.25::numeric,
                1.00000000::numeric
            ),
            (
                'AAPL',
                1,
                178.00::numeric,
                10.50000000::numeric
            ),
            (
                'AAPL',
                2,
                181.25::numeric,
                10.45000000::numeric
            ),
            (
                'AAPL',
                3,
                179.75::numeric,
                10.48000000::numeric
            ),
            (
                'SWED-A',
                1,
                202.00::numeric,
                1.00000000::numeric
            ),
            (
                'SWED-A',
                2,
                205.50::numeric,
                1.00000000::numeric
            ),
            (
                'SWED-A',
                3,
                204.00::numeric,
                1.00000000::numeric
            ),
            (
                'SAND',
                1,
                215.00::numeric,
                1.00000000::numeric
            ),
            (
                'SAND',
                2,
                219.00::numeric,
                1.00000000::numeric
            ),
            (
                'SAND',
                3,
                217.50::numeric,
                1.00000000::numeric
            )
    )
INSERT INTO
    position_snapshot (
        user_id,
        account_id,
        ticker,
        price_date,
        quantity,
        close_price,
        currency,
        currency_fx,
        sek_value,
        target_allocation
    )
SELECT demo_user.id, account.id, holding.ticker, demo_date.price_date, holding.quantity, demo_price.close_price, holding.currency, demo_price.currency_fx, holding.quantity * demo_price.close_price * demo_price.currency_fx, target.target_pct
FROM
    users AS demo_user
    JOIN accounts AS account ON account.user_id = demo_user.id
    JOIN holdings AS holding ON holding.account_id = account.id
    JOIN demo_prices AS demo_price ON demo_price.ticker = holding.ticker
    JOIN demo_dates AS demo_date ON demo_date.day_number = demo_price.day_number
    LEFT JOIN target_allocations AS target ON target.user_id = demo_user.id
    AND target.account_type = account.account_type
WHERE
    demo_user.email = 'anna@example.com'
    AND NOT EXISTS (
        SELECT 1
        FROM position_snapshot AS existing
        WHERE
            existing.user_id = demo_user.id
    );