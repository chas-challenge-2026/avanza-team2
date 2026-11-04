ALTER TABLE target_allocations
    ADD COLUMN IF NOT EXISTS account_id INT REFERENCES accounts(id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_target_allocations_account_type
    ON target_allocations (account_id, account_type)
    WHERE account_id IS NOT NULL;

INSERT INTO target_allocations (user_id, account_id, account_type, target_pct)
SELECT account.user_id, account.id, allocation.account_type, allocation.target_pct
FROM accounts AS account
CROSS JOIN (
    VALUES ('Aktier', 50.00), ('Fonder', 50.00)
) AS allocation(account_type, target_pct)
WHERE NOT EXISTS (
    SELECT 1
    FROM target_allocations AS existing
    WHERE existing.account_id = account.id
      AND existing.account_type = allocation.account_type
);
