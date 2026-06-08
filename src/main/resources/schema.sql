CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS loan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID        NOT NULL,
    account_id      UUID        NOT NULL,
    loan_type       VARCHAR(50) NOT NULL,
    loan_status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    principal_amount NUMERIC(15, 2) NOT NULL,
    remaining_balance NUMERIC(15, 2) NOT NULL,
    interest_rate   NUMERIC(5, 4) NOT NULL,
    term_months     INTEGER     NOT NULL,
    monthly_payment NUMERIC(15, 2) NOT NULL,
    start_date      DATE        NOT NULL,
    end_date        DATE        NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
