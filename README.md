# Expense Manager

Full-stack Personal Expense Management Application with JWT authentication.

## Features

- **User Authentication**: Register/Login with JWT tokens
- **Money Sources**: Manage multiple accounts (Cash, Bank, E-Wallet, Credit Card)
- **Monthly Balance**: Set opening balance for each month
- **Categories**: Create income/expense categories with icons and colors
- **Transactions**:
  - Create transactions (status: PENDING)
  - **Confirmation Flow**: Confirm transactions to update money source balance
  - Cancel pending transactions
  - Delete transactions

## Tech Stack

**Backend**: Spring Boot 3.3 + PostgreSQL + JWT
**Frontend**: React + Vite + TailwindCSS + Axios

## Setup

### 1. Database (PostgreSQL)

```sql
CREATE DATABASE expense_db;
```

### 2. Backend

```bash
cd expense-manager
./mvnw install
./mvnw spring-boot:run
```

Backend runs on: `http://localhost:8080`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:5173`

## API Endpoints

### Auth
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Get current user

### Dashboard
- `GET /api/dashboard` - Get dashboard data

### Money Sources
- `GET /api/money-sources` - List all
- `POST /api/money-sources` - Create
- `PUT /api/money-sources/{id}` - Update
- `DELETE /api/money-sources/{id}` - Delete

### Monthly Balances
- `GET /api/monthly-balances` - List all
- `GET /api/monthly-balances/current` - Get current month
- `POST /api/monthly-balances` - Create/Update

### Categories
- `GET /api/categories` - List all (optional: `?type=INCOME|EXPENSE`)
- `POST /api/categories` - Create
- `PUT /api/categories/{id}` - Update
- `DELETE /api/categories/{id}` - Delete

### Transactions
- `GET /api/transactions` - List all (paginated)
- `GET /api/transactions/pending` - List pending
- `POST /api/transactions` - Create (PENDING)
- `POST /api/transactions/{id}/confirm` - Confirm & update balance
- `POST /api/transactions/{id}/cancel` - Cancel
- `DELETE /api/transactions/{id}` - Delete

## Transaction Flow

1. **Create**: User creates transaction → Status = PENDING
2. **Confirm**: User confirms → Money source balance updated
3. **Cancel**: User cancels → Status = CANCELLED

## Project Structure

```
expense-manager/
├── expense-manager/          # Backend (Spring Boot)
│   └── src/main/java/
│       └── com/dev/expense_manager/
│           ├── config/       # Security, CORS config
│           ├── controller/   # REST controllers
│           ├── dto/          # Request/Response DTOs
│           ├── entity/       # JPA entities
│           ├── exception/    # Exception handlers
│           ├── repository/   # JPA repositories
│           ├── security/     # JWT components
│           └── service/      # Business logic
│
└── frontend/                # Frontend (React)
    └── src/
        ├── components/       # Layout components
        ├── pages/           # Page components
        └── services/        # API service
```
