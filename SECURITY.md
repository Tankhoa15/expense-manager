# Security Features

## Authentication & Authorization

### JWT-based Authentication
- Token-based stateless authentication
- Configurable expiration (default: 24 hours)
- Token validated on every request via `JwtAuthenticationFilter`
- BCrypt password hashing (strength: 10)

### Password Policy
Strong password validation enforced on registration:
- ✅ Minimum 8 characters
- ✅ At least 1 uppercase letter (A-Z)
- ✅ At least 1 lowercase letter (a-z)
- ✅ At least 1 digit (0-9)
- ✅ At least 1 special character (!@#$%^&*()_+-=[]{};':"\\|,.<>/?)

**Example valid passwords:**
- `MyPass123!`
- `Test@2024Secure`
- `Admin#Pass1`

**Example invalid passwords:**
- `test123` ❌ (no uppercase, no special char)
- `TEST123!` ❌ (no lowercase)
- `TestPass` ❌ (no digit, no special char)

## Rate Limiting

### Authentication Endpoints Protection
Prevents brute-force attacks on login and registration:

- **Endpoints**: `/api/v1/auth/login`, `/api/v1/auth/register`
- **Limit**: 5 requests per minute per IP address
- **Response**: HTTP 429 Too Many Requests when exceeded
- **Detection**: Supports X-Forwarded-For and X-Real-IP headers for reverse proxy setups

### Implementation Details
- In-memory rate limiting using `ConcurrentHashMap`
- Automatic cleanup of old entries
- IP-based tracking with header support for load balancers

## CORS Configuration

### Dynamic Origins
CORS origins are configurable per environment:

```yaml
# Development
CORS_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:8080

# Production
CORS_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

### CORS Settings
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: All (*)
- **Allow Credentials**: true
- **Max Age**: 3600 seconds (1 hour)

## Secret Management

### JWT Secret
- **Development**: Default value for convenience
- **UAT**: Recommended to set custom secret
- **Production**: **REQUIRED** (no default fallback)

**Generate strong secret:**
```bash
openssl rand -base64 64
```

### Environment-Specific Secrets
Never commit secrets to version control:
- Use `.env.prod` for production (gitignored)
- Use `.env.uat` for UAT (gitignored)
- Rotate secrets regularly

## Session Management

### Stateless Sessions
- No server-side session storage
- JWT contains all necessary user information
- Redis used only for caching, not session state

## Data Protection

### Soft Delete
Sensitive data is never hard-deleted:
- Users, transactions, categories, budgets use `is_deleted` flag
- Enables data recovery and audit trails
- Database constraints prevent cascading deletes of critical data

### SQL Injection Prevention
- All queries use JPA/Hibernate with parameterized queries
- No raw SQL with string concatenation
- Repository pattern abstracts database access

## API Security

### Endpoint Protection
All API endpoints (except auth and health) require:
- Valid JWT token in `Authorization: Bearer <token>` header
- User must own the resources they're accessing (user ID validation)

### Public Endpoints
Only these endpoints are accessible without authentication:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/health`

## Security Headers

### Recommended Nginx Configuration
If deploying behind Nginx, add these headers:

```nginx
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
```

## Monitoring & Logging

### Security Events Logged
- Failed login attempts (via Spring Security)
- Rate limit violations (IP + endpoint)
- Authentication errors
- Resource not found (potential enumeration attacks)

### Log Levels
- **Development**: DEBUG
- **UAT**: INFO
- **Production**: WARN/ERROR only

## Best Practices

### Deployment Checklist
- [ ] Set strong `JWT_SECRET` (64+ characters)
- [ ] Configure `CORS_ORIGINS` for your domain
- [ ] Set strong database password
- [ ] Set Redis password
- [ ] Use HTTPS in production
- [ ] Enable firewall rules
- [ ] Review and rotate secrets regularly
- [ ] Monitor failed authentication attempts
- [ ] Keep dependencies up to date

### Development Guidelines
- Never commit `.env.prod` or `.env.uat`
- Use environment variables for all secrets
- Test password policy in integration tests
- Validate user ownership in all service methods
- Use constants from `CacheKeyConstants` for cache keys
- Apply `@Transactional` with appropriate isolation levels

## Vulnerability Disclosure

If you discover a security vulnerability, please email: [your-security-email@example.com]

Do not open public GitHub issues for security vulnerabilities.
