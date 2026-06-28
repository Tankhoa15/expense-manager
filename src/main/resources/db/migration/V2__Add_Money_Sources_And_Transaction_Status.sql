-- V2: Add user_sessions, 2FA columns, money_sources, monthly_balances, transaction status

-- User sessions table
CREATE TABLE user_sessions (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    device_info VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    last_accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_active ON user_sessions(user_id, is_active);

-- 2FA columns for users
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS backup_codes TEXT;

-- Money source type enum
CREATE TYPE money_source_type AS ENUM ('CASH', 'BANK_ACCOUNT', 'CREDIT_CARD', 'E_WALLET', 'OTHER');

-- Money sources table
CREATE TABLE money_sources (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    name VARCHAR(100) NOT NULL,
    source_type money_source_type NOT NULL DEFAULT 'CASH',
    initial_balance DECIMAL(15, 2) NOT NULL DEFAULT 0,
    current_balance DECIMAL(15, 2) NOT NULL DEFAULT 0,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_money_sources_user_id ON money_sources(user_id);
CREATE INDEX idx_money_sources_is_deleted ON money_sources(is_deleted);

-- Monthly balances table (opening balance per month, totals computed from transactions)
CREATE TABLE monthly_balances (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    opening_balance DECIMAL(15, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, year, month)
);

CREATE INDEX idx_monthly_balances_user_id ON monthly_balances(user_id);
CREATE INDEX idx_monthly_balances_year_month ON monthly_balances(user_id, year, month);

-- Transaction status enum
CREATE TYPE transaction_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED');

-- Add status and money_source_id to transactions
ALTER TABLE transactions
    ADD COLUMN status transaction_status NOT NULL DEFAULT 'CONFIRMED',
    ADD COLUMN money_source_id VARCHAR(36) REFERENCES money_sources(id) ON DELETE SET NULL;

CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_money_source ON transactions(money_source_id);
