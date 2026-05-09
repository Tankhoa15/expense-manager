# Expense Manager API

## Setup

### 1. Database Setup (PostgreSQL)

```sql
CREATE DATABASE expense_manager;
```

### 2. Environment Variables

Create a `.env` file or set environment variables:

```bash
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
export GITHUB_CLIENT_ID=your_github_client_id
export GITHUB_CLIENT_SECRET=your_github_client_secret
```

### 3. Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

## API Endpoints

### Health Check
- `GET /api/health` - Check API health

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user (requires auth)

### Categories
- `POST /api/categories/seed` - Seed default categories
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/type/{type}` - Get categories by type (INCOME/EXPENSE)
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category (soft delete)

### Transactions
- `GET /api/transactions` - Get all transactions (paginated)
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions/date-range?startDate=&endDate=` - Get transactions by date range
- `GET /api/transactions/summary?startDate=&endDate=` - Get dashboard summary
- `POST /api/transactions` - Create transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction (soft delete)

### Budgets
- `GET /api/budgets` - Get all budgets
- `GET /api/budgets/{id}` - Get budget by ID
- `GET /api/budgets/alerts` - Get budget alerts
- `POST /api/budgets` - Create budget
- `PUT /api/budgets/{id}` - Update budget
- `DELETE /api/budgets/{id}` - Delete budget (soft delete)
- `POST /api/budgets/recalculate` - Recalculate budget spending

## Testing with Postman

1. Import `ExpenseManager_API.postman_collection.json` into Postman
2. Run `Register` to create a user
3. Run `Login` - token will be auto-saved to collection variable
4. Run `Seed Default Categories` to create categories
5. Now you can test all other endpoints

## Sample Requests

### Register
```json
{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
}
```

### Login
```json
{
    "email": "test@example.com",
    "password": "password123"
}
```

### Create Category
```json
{
    "name": "Groceries",
    "icon": "🛒",
    "color": "#FF9800",
    "type": "EXPENSE"
}
```

### Create Transaction
```json
{
    "amount": 500000,
    "type": "EXPENSE",
    "transactionDate": "2026-05-09",
    "categoryId": "category-uuid",
    "note": "Weekly groceries",
    "description": "Bought groceries for the week"
}
```

### Create Budget
```json
{
    "amount": 5000000,
    "period": "MONTHLY",
    "periodStart": "2026-05-01",
    "periodEnd": "2026-05-31",
    "categoryId": "category-uuid",
    "alertThreshold": 80
}
```
