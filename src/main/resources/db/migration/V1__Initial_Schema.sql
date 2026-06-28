-- V1__Initial_Schema.sql
-- Users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    full_name VARCHAR(255),
    avatar_url VARCHAR(500),
    provider VARCHAR(50),
    provider_id VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- System user for default categories copied to newly registered users
INSERT INTO users (id, email, full_name, email_verified)
VALUES ('00000000-0000-0000-0000-000000000000', 'system@expense-manager.local', 'System', TRUE);

-- Categories table
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(7),
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_user_type ON categories(user_id, type);
CREATE INDEX idx_categories_is_deleted ON categories(is_deleted);

-- Transactions table
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    amount DECIMAL(15, 2) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    transaction_date DATE NOT NULL,
    note VARCHAR(500),
    description VARCHAR(1000),
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id VARCHAR(36) NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_is_deleted ON transactions(is_deleted);

-- Budgets table
CREATE TABLE budgets (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    amount DECIMAL(15, 2) NOT NULL,
    spent_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    period VARCHAR(20) NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id VARCHAR(36) REFERENCES categories(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    alert_threshold INTEGER NOT NULL DEFAULT 80,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category ON budgets(category_id);
CREATE INDEX idx_budgets_period ON budgets(user_id, period_start, period_end);
CREATE INDEX idx_budgets_is_active ON budgets(is_active);
CREATE INDEX idx_budgets_is_deleted ON budgets(is_deleted);

-- Insert default categories for new users (these will be copied when user is created)
INSERT INTO categories (name, icon, color, type, user_id, is_default) VALUES
('Salary', '💰', '#4CAF50', 'INCOME', '00000000-0000-0000-0000-000000000000', TRUE),
('Investment', '📈', '#2196F3', 'INCOME', '00000000-0000-0000-0000-000000000000', TRUE),
('Side Income', '💵', '#9C27B0', 'INCOME', '00000000-0000-0000-0000-000000000000', TRUE),
('Gift', '🎁', '#E91E63', 'INCOME', '00000000-0000-0000-0000-000000000000', TRUE),
('Food & Dining', '🍽️', '#FF5722', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE),
('Transportation', '🚗', '#607D8B', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE),
('Shopping', '🛍️', '#E91E63', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE),
('Entertainment', '🎮', '#9C27B0', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE),
('Bills & Utilities', '💡', '#FFC107', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE),
('Healthcare', '🏥', '#F44336', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE),
('Education', '📚', '#3F51B5', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE),
('Other Expense', '📦', '#795548', 'EXPENSE', '00000000-0000-0000-0000-000000000000', TRUE);
