-- V3: Drop unused features (RabbitMQ messaging had no schema; 2FA and user-sessions never wired)

-- Remove unused 2FA columns from users
ALTER TABLE users DROP COLUMN IF EXISTS two_factor_enabled;
ALTER TABLE users DROP COLUMN IF EXISTS two_factor_secret;
ALTER TABLE users DROP COLUMN IF EXISTS backup_codes;

-- Remove unused user_sessions table (session management was never integrated)
DROP TABLE IF EXISTS user_sessions;
