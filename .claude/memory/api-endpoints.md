---
name: api-endpoints
description: Danh sách API endpoints
metadata:
  type: reference
  domain: api
---

## API Endpoints

All endpoints prefix `/api/v1`. All authenticated (except `/auth/**` and `/health`).

### Auth (`/api/v1/auth`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register` | No | Register (email, password, fullName) |
| POST | `/login` | No | Login (email, password) → token |
| GET | `/me` | Yes | Current user info |

### Dashboard (`/api/v1/dashboard`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Full dashboard (totalBalance, monthIncome/Expense, pendingAmount, sources, recent Txns, category breakdown) |

### Categories (`/api/v1/categories`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | All categories |
| GET | `/{id}` | By ID |
| GET | `/type/{type}` | By TransactionType |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Soft delete |
| POST | `/seed` | Seed defaults |

### Transactions (`/api/v1/transactions`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Paginated list |
| GET | `/pending` | PENDING only, paginated |
| GET | `/{id}` | By ID |
| GET | `/date-range` | By date range |
| POST | `/` | Create (PENDING) |
| PUT | `/{id}` | Update (only if PENDING) |
| POST | `/{id}/confirm` | Confirm → update balance |
| POST | `/{id}/cancel` | Cancel |
| DELETE | `/{id}` | Soft delete |

### Budgets (`/api/v1/budgets`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | All budgets |
| GET | `/{id}` | By ID |
| GET | `/alerts` | Budgets exceeding threshold |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Soft delete |
| POST | `/recalculate` | Recalculate all active |

### Money Sources (`/api/v1/money-sources`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | All |
| GET | `/{id}` | By ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update (adjusts balance) |
| DELETE | `/{id}` | Soft delete |

### Monthly Balances (`/api/v1/monthly-balances`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | All (desc year/month) |
| GET | `/current` | Current month |
| POST | `/` | Create or update |

### Monitor (`/api/v1/monitor`) — cached via Redis
| Method | Path | Description |
|--------|------|-------------|
| GET | `/overview` | Today/week/month counts |
| GET | `/recent` | Last 50 transactions |
| GET | `/statistics` | Category breakdown |
| GET | `/trend?days=30` | Daily trend |

### Health (`/api/v1/health`) — public
| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |

**Why**: API reference for development and documentation.

**How to apply**: Dùng làm quick reference khi implement controller hoặc gọi API từ frontend.
