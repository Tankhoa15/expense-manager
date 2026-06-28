# Migration Guide: From `/api/*` to `/api/v1/*`

## Overview

All API endpoints have been migrated to include version prefix `/api/v1/` for better API lifecycle management and backward compatibility support.

## Timeline

- **Old API** (`/api/*`): Deprecated as of 2024-06-28
- **New API** (`/api/v1/*`): Current version

## Frontend Changes Required

### Update Base URL

**File**: `frontend/src/services/api.js` (or your API configuration file)

```javascript
// Before
const API_BASE_URL = '/api';

// After
const API_BASE_URL = '/api/v1';
```

### Axios Instance Example

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',  // ← Update this line
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token interceptor
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

## Complete Endpoint Mapping

### Authentication
| Old Endpoint | New Endpoint | Notes |
|--------------|--------------|-------|
| `POST /api/auth/register` | `POST /api/v1/auth/register` | Password policy now enforced |
| `POST /api/auth/login` | `POST /api/v1/auth/login` | Rate limited: 5 req/min |
| `GET /api/auth/me` | `GET /api/v1/auth/me` | — |

### Dashboard
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/dashboard` | `GET /api/v1/dashboard` |

### Transactions
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/transactions` | `GET /api/v1/transactions` |
| `POST /api/transactions` | `POST /api/v1/transactions` |
| `GET /api/transactions/{id}` | `GET /api/v1/transactions/{id}` |
| `PUT /api/transactions/{id}` | `PUT /api/v1/transactions/{id}` |
| `DELETE /api/transactions/{id}` | `DELETE /api/v1/transactions/{id}` |
| `GET /api/transactions/pending` | `GET /api/v1/transactions/pending` |
| `GET /api/transactions/date-range` | `GET /api/v1/transactions/date-range` |
| `POST /api/transactions/{id}/confirm` | `POST /api/v1/transactions/{id}/confirm` |
| `POST /api/transactions/{id}/cancel` | `POST /api/v1/transactions/{id}/cancel` |

### Categories
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/categories` | `GET /api/v1/categories` |
| `POST /api/categories` | `POST /api/v1/categories` |
| `GET /api/categories/{id}` | `GET /api/v1/categories/{id}` |
| `PUT /api/categories/{id}` | `PUT /api/v1/categories/{id}` |
| `DELETE /api/categories/{id}` | `DELETE /api/v1/categories/{id}` |
| `GET /api/categories/type/{type}` | `GET /api/v1/categories/type/{type}` |
| `POST /api/categories/seed` | `POST /api/v1/categories/seed` |

### Money Sources
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/money-sources` | `GET /api/v1/money-sources` |
| `POST /api/money-sources` | `POST /api/v1/money-sources` |
| `GET /api/money-sources/{id}` | `GET /api/v1/money-sources/{id}` |
| `PUT /api/money-sources/{id}` | `PUT /api/v1/money-sources/{id}` |
| `DELETE /api/money-sources/{id}` | `DELETE /api/v1/money-sources/{id}` |

### Budgets
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/budgets` | `GET /api/v1/budgets` |
| `POST /api/budgets` | `POST /api/v1/budgets` |
| `GET /api/budgets/{id}` | `GET /api/v1/budgets/{id}` |
| `PUT /api/budgets/{id}` | `PUT /api/v1/budgets/{id}` |
| `DELETE /api/budgets/{id}` | `DELETE /api/v1/budgets/{id}` |
| `GET /api/budgets/alerts` | `GET /api/v1/budgets/alerts` |
| `POST /api/budgets/recalculate` | `POST /api/v1/budgets/recalculate` |

### Monthly Balances
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/monthly-balances` | `GET /api/v1/monthly-balances` |
| `POST /api/monthly-balances` | `POST /api/v1/monthly-balances` |
| `GET /api/monthly-balances/current` | `GET /api/v1/monthly-balances/current` |

### Monitor
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/monitor/overview` | `GET /api/v1/monitor/overview` |
| `GET /api/monitor/recent` | `GET /api/v1/monitor/recent` |
| `GET /api/monitor/statistics` | `GET /api/v1/monitor/statistics` |
| `GET /api/monitor/trend` | `GET /api/v1/monitor/trend` |

### Health
| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/health` | `GET /api/v1/health` |

## Testing Your Migration

### 1. Test Health Endpoint (No Auth Required)
```bash
curl http://localhost:8080/api/v1/health
```

Expected response:
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "timestamp": "2024-06-28T...",
    "service": "expense-manager"
  },
  "timestamp": "..."
}
```

### 2. Test Register (Note: Password Policy)
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#",
    "fullName": "Test User"
  }'
```

⚠️ **Password must include**:
- At least 8 characters
- Uppercase + lowercase + digit + special character

### 3. Test Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#"
  }'
```

### 4. Test Rate Limiting
Try logging in 6 times rapidly:
```bash
for i in {1..6}; do
  echo "Attempt $i:"
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"wrong@example.com","password":"wrong"}' \
    -w "\nHTTP Status: %{http_code}\n\n"
  sleep 0.5
done
```

Expected: First 5 attempts return 401, 6th attempt returns **429 Too Many Requests**.

## Backward Compatibility

❌ **No backward compatibility** - Old `/api/*` endpoints are removed.

All clients must update to `/api/v1/*` endpoints.

## Benefits of API Versioning

1. **Future-proof**: Can introduce `/api/v2/` without breaking existing clients
2. **Clear deprecation path**: Easier to communicate API changes
3. **Better documentation**: Version-specific documentation
4. **Gradual migration**: Can support multiple versions simultaneously in the future

## Common Issues

### Issue: 404 Not Found
**Cause**: Still using old `/api/*` endpoint  
**Solution**: Update to `/api/v1/*`

### Issue: 429 Too Many Requests on Login
**Cause**: Rate limiting (5 requests per minute)  
**Solution**: Wait 60 seconds before retrying, or ensure frontend doesn't retry failed logins too quickly

### Issue: 400 Bad Request on Register
**Cause**: Password doesn't meet new policy requirements  
**Solution**: Ensure password has 8+ chars, uppercase, lowercase, digit, and special character

## Need Help?

- Check `README.md` for full API documentation
- See `SECURITY.md` for security features
- Review `CHANGELOG.md` for all changes
