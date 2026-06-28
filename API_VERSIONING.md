# API Versioning Migration Guide

## Changes Made

All API endpoints have been migrated from `/api/*` to `/api/v1/*`.

### Endpoint Mapping

| Old Endpoint | New Endpoint |
|--------------|--------------|
| `/api/auth/**` | `/api/v1/auth/**` |
| `/api/health` | `/api/v1/health` |
| `/api/dashboard` | `/api/v1/dashboard` |
| `/api/transactions/**` | `/api/v1/transactions/**` |
| `/api/categories/**` | `/api/v1/categories/**` |
| `/api/money-sources/**` | `/api/v1/money-sources/**` |
| `/api/budgets/**` | `/api/v1/budgets/**` |
| `/api/monthly-balances/**` | `/api/v1/monthly-balances/**` |
| `/api/monitor/**` | `/api/v1/monitor/**` |

## Frontend Update Required

Update your frontend API calls to use the new `/api/v1/` prefix.

Example (in `frontend/src/services/api.js`):
```javascript
// OLD
const API_BASE_URL = '/api';

// NEW
const API_BASE_URL = '/api/v1';
```

## Testing

Test endpoints with the new version:
```bash
curl http://localhost:8080/api/v1/health
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'
```
