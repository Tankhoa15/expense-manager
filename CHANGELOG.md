# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **API Versioning**: All endpoints now use `/api/v1/` prefix for future compatibility
- **Password Policy Validation**: Strong password requirements enforced on registration
  - Minimum 8 characters
  - At least 1 uppercase, 1 lowercase, 1 digit, 1 special character
- **Rate Limiting**: Authentication endpoints protected against brute-force attacks
  - 5 requests per minute per IP address
  - HTTP 429 response when exceeded
- **Cache Key Constants**: Centralized cache key management via `CacheKeyConstants` class
- **CORS Configuration**: Dynamic CORS origins via environment variable `CORS_ORIGINS`
- **Documentation**: 
  - `SECURITY.md` - Security features and best practices
  - `API_VERSIONING.md` - Migration guide for API changes
  - `FIXES_SUMMARY.md` - Summary of all improvements

### Changed
- **SecurityConfig**: CORS origins now configurable per environment instead of hardcoded
- **JWT Secret**: Production config no longer has default value (must be set via environment)
- **TransactionMonitorServiceImpl**: Fixed unchecked operations warnings with proper type casting
- **Cache Eviction**: Services now use constants instead of hardcoded strings

### Fixed
- **BudgetRequest**: Added `@Builder.Default` to `alertThreshold` field to fix Lombok warning
- **RateLimitFilter**: Updated paths to match new `/api/v1/` versioning
- **Application Configs**: All environment configs now include CORS settings

### Security
- JWT secret required in production (no fallback)
- Password strength validation
- Rate limiting on authentication endpoints
- Configurable CORS origins

## [0.0.1-SNAPSHOT] - 2024-06-28

### Initial Release
- Full CRUD for Transactions, Categories, Money Sources, Budgets
- JWT authentication
- PostgreSQL with Flyway migrations
- Redis caching for dashboard and statistics
- Email notifications for budget alerts
- Soft delete pattern
- Transaction status workflow (PENDING → CONFIRMED → CANCELLED)
- Docker support with compose files for local/uat/prod
