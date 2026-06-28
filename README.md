# Expense Manager

Ứng dụng quản lý chi tiêu cá nhân, full-stack. Backend Spring Boot 4 + PostgreSQL + Redis. Frontend React + Vite + Tailwind CSS.

> 📐 Chi tiết kiến trúc backend theo tầng (DB → Entity → Repository → Service → API): xem [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## Mục lục

1. [Tổng quan kiến trúc](#tổng-quan-kiến-trúc)
2. [Tech Stack](#tech-stack)
3. [Cài đặt & Chạy local](#cài-đặt--chạy-local)
4. [Cấu trúc project](#cấu-trúc-project)
5. [Domain & Data model](#domain--data-model)
6. [Transaction flow](#transaction-flow)
7. [API Reference](#api-reference)
8. [Biến môi trường](#biến-môi-trường)
9. [Deploy](#deploy)
10. [Lưu ý khi phát triển](#lưu-ý-khi-phát-triển)

---

## Tổng quan kiến trúc

```
Browser (React SPA)
       │  HTTP + JWT
       ▼
  Spring Boot 8080
       │
       ├── PostgreSQL 5432   (dữ liệu chính, Flyway migration)
       └── Redis 6379         (cache cho các endpoint /api/monitor)
```

**Request flow điển hình:**
1. Frontend gửi request kèm `Authorization: Bearer <JWT>`
2. `JwtAuthenticationFilter` validate token, set `UserPrincipal` vào SecurityContext
3. Controller gọi Service → Repository → PostgreSQL
4. Khi tạo/sửa/xóa transaction: invalidate Redis cache (dashboard/statistics)
5. Khi budget vượt `alertThreshold`: gửi email cảnh báo bất đồng bộ (`@Async`)

---

## Tech Stack

| Layer | Công nghệ | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.4 |
| Database | PostgreSQL | 16 |
| Migration | Flyway | (managed by Spring Boot) |
| Cache | Redis | 7 |
| Auth | JWT (jjwt) | 0.12.5 |
| Build | Maven Wrapper | — |
| Frontend | React + Vite | Node 20 |
| Styling | Tailwind CSS | 3 |
| HTTP Client | Axios | — |
| Container | Docker + docker-compose | — |

---

## Cài đặt & Chạy local

### Yêu cầu

- Java 21+
- Maven (hoặc dùng `./mvnw` trong repo)
- Node 20+
- Docker & Docker Compose (để chạy infra)
- PostgreSQL, Redis (hoặc dùng Docker)

### 1. Khởi động infrastructure

```bash
# Chạy PostgreSQL + Redis bằng Docker
docker-compose up -d postgres redis
```

Sau khi khởi động:
- PostgreSQL: `localhost:5432`, database `expense_db`, user `postgres`, password `123456`
- Redis: `localhost:6379`

### 2. Chạy Backend

```bash
# Từ thư mục gốc
./mvnw spring-boot:run
```

Backend chạy tại `http://localhost:8080`.

Flyway tự động chạy migration khi khởi động:
- `V1__Initial_Schema.sql` — tạo bảng users, categories, transactions, budgets
- `V2__Add_Money_Sources_And_Transaction_Status.sql` — thêm money_sources, monthly_balances, transaction status
- `V3__Drop_Unused_Features.sql` — gỡ cột 2FA và bảng user_sessions (chưa từng dùng)

### 3. Chạy Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy tại `http://localhost:5173`.

> Vite proxy `/api` → `http://localhost:8080` nên không cần cấu hình CORS thêm.

---

## Cấu trúc project

```
expense-manager/
├── src/main/
│   ├── java/com/dev/expense_manager/
│   │   ├── config/           # SecurityConfig, RedisConfig, AsyncConfig
│   │   ├── controller/       # REST controllers (1 file / domain)
│   │   ├── dto/
│   │   │   ├── request/      # Input DTOs (validated)
│   │   │   └── response/     # Output DTOs
│   │   ├── entity/           # JPA entities + enums
│   │   ├── exception/        # GlobalExceptionHandler, custom exceptions
│   │   ├── mapper/           # Entity ↔ DTO (plain @Component, không dùng MapStruct)
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JWT filter, UserPrincipal, UserDetailsService
│   │   └── service/
│   │       ├── *.java        # Service interfaces
│   │       └── impl/         # Service implementations
│   └── resources/
│       ├── application.yaml          # Config mặc định (local dev)
│       ├── application-uat.yaml      # Override cho môi trường UAT
│       ├── application-prod.yaml     # Override cho môi trường PROD
│       └── db/migration/
│           ├── V1__Initial_Schema.sql
│           ├── V2__Add_Money_Sources_And_Transaction_Status.sql
│           └── V3__Drop_Unused_Features.sql
│
├── frontend/
│   ├── src/
│   │   ├── components/       # Layout.jsx (sidebar navigation)
│   │   ├── pages/            # Dashboard, Transactions, Budgets, Categories, ...
│   │   └── services/
│   │       └── api.js        # Axios instance + tất cả service objects
│   ├── Dockerfile
│   └── nginx.conf            # SPA config + reverse proxy /api/
│
├── Dockerfile                # Backend multi-stage build
├── docker-compose.yml        # Local dev (tất cả services)
├── docker-compose.uat.yml    # UAT
├── docker-compose.prod.yml   # Production
├── .env.uat.example          # Template env vars cho UAT
└── .env.prod.example         # Template env vars cho PROD
```

---

## Domain & Data model

### Entities và quan hệ

```
User (1) ─── (N) Category
User (1) ─── (N) MoneySource
User (1) ─── (N) Transaction ───► Category
                                └──► MoneySource (optional)
User (1) ─── (N) Budget ───► Category (optional)
User (1) ─── (N) MonthlyBalance
```

### Mô tả từng entity

| Entity | Bảng | Mô tả |
|---|---|---|
| `User` | `users` | Tài khoản người dùng, provider=local (email/pass) |
| `Category` | `categories` | Phân loại thu/chi, có icon + color, soft-delete |
| `MoneySource` | `money_sources` | Ví tiền (CASH / BANK_ACCOUNT / CREDIT_CARD / E_WALLET / OTHER), lưu `currentBalance` |
| `Transaction` | `transactions` | Giao dịch, có status PENDING/CONFIRMED/CANCELLED, soft-delete |
| `Budget` | `budgets` | Ngân sách theo kỳ (DAILY/WEEKLY/MONTHLY/YEARLY), tính `spentAmount` và `percentageUsed` |
| `MonthlyBalance` | `monthly_balances` | Opening balance theo tháng, income/expense tính từ transaction CONFIRMED |

### Enums

```java
TransactionType   : INCOME, EXPENSE
TransactionStatus : PENDING, CONFIRMED, CANCELLED
BudgetPeriod      : DAILY, WEEKLY, MONTHLY, YEARLY
MoneySourceType   : CASH, BANK_ACCOUNT, CREDIT_CARD, E_WALLET, OTHER
```

### Soft delete

Hầu hết entity có `is_deleted = false`. Khi xóa, set `isDeleted = true` thay vì DELETE khỏi DB. Tất cả query đều có điều kiện `AND is_deleted = false`.

---

## Transaction flow

Giao dịch có 3 trạng thái:

```
     create
       │
    PENDING  ──confirm──►  CONFIRMED  (balance MoneySource được cập nhật)
       │
    cancel
       │
   CANCELLED
```

**Confirm INCOME**: `moneySource.currentBalance += amount`  
**Confirm EXPENSE**: `moneySource.currentBalance -= amount`  
**Xóa CONFIRMED transaction**: hoàn lại balance (reverse)  
**Budget spending**: chỉ tính transaction có status = CONFIRMED

---

## API Reference

> Tất cả API (trừ auth + health) yêu cầu header: `Authorization: Bearer <token>`  
> Response format: `{ success: boolean, message: string, data: T, timestamp: ... }`

### Auth — `/api/auth`

| Method | Path | Body | Mô tả |
|---|---|---|---|
| POST | `/register` | `{ email, password, fullName }` | Đăng ký (tự tạo default categories + ví Cash) |
| POST | `/login` | `{ email, password }` | Trả về `{ accessToken, tokenType, user }` |
| GET | `/me` | — | Thông tin user hiện tại |

### Dashboard — `/api/dashboard`

| Method | Path | Mô tả |
|---|---|---|
| GET | `/` | `totalBalance`, `monthIncome`, `monthExpense`, `pendingAmount`, `moneySources[]`, `recentTransactions[]`, `categoryBreakdown[]` |

### Money Sources — `/api/money-sources`

| Method | Path | Body | Mô tả |
|---|---|---|---|
| GET | `/` | — | Danh sách ví |
| GET | `/{id}` | — | Chi tiết ví |
| POST | `/` | `{ name, sourceType, initialBalance }` | Tạo ví |
| PUT | `/{id}` | `{ name, sourceType, initialBalance }` | Cập nhật (điều chỉnh currentBalance tương ứng) |
| DELETE | `/{id}` | — | Soft delete |

### Monthly Balances — `/api/monthly-balances`

| Method | Path | Body | Mô tả |
|---|---|---|---|
| GET | `/` | — | Lịch sử số dư (totalIncome/Expense tính từ CONFIRMED transactions) |
| GET | `/current` | — | Tháng hiện tại |
| POST | `/` | `{ year, month, openingBalance }` | Tạo/cập nhật opening balance |

### Categories — `/api/categories`

| Method | Path | Body | Mô tả |
|---|---|---|---|
| GET | `/` | — | Tất cả (có thể thêm `?type=INCOME\|EXPENSE`) |
| GET | `/{id}` | — | Chi tiết |
| GET | `/type/{type}` | — | Lọc theo type |
| POST | `/` | `{ name, icon, color, type }` | Tạo |
| PUT | `/{id}` | `{ name, icon, color, type }` | Cập nhật |
| DELETE | `/{id}` | — | Soft delete |
| POST | `/seed` | — | Tạo default categories |

### Transactions — `/api/transactions`

| Method | Path | Body | Mô tả |
|---|---|---|---|
| GET | `/` | — | Tất cả (page/size query params) |
| GET | `/pending` | — | Chỉ PENDING transactions |
| GET | `/{id}` | — | Chi tiết |
| GET | `/date-range` | — | `?startDate=&endDate=` |
| POST | `/` | `{ amount, type, categoryId, moneySourceId?, transactionDate, note?, description? }` | Tạo (status=PENDING) |
| PUT | `/{id}` | — | Cập nhật (chỉ khi PENDING) |
| POST | `/{id}/confirm` | — | Xác nhận, cập nhật balance |
| POST | `/{id}/cancel` | — | Huỷ |
| DELETE | `/{id}` | — | Xóa (nếu đã CONFIRMED, hoàn lại balance) |

### Budgets — `/api/budgets`

| Method | Path | Body | Mô tả |
|---|---|---|---|
| GET | `/` | — | Tất cả (kèm spentAmount tính realtime) |
| GET | `/{id}` | — | Chi tiết |
| GET | `/alerts` | — | Budgets vượt alertThreshold |
| POST | `/` | `{ amount, period, periodStart, periodEnd, categoryId?, alertThreshold }` | Tạo |
| PUT | `/{id}` | — | Cập nhật |
| DELETE | `/{id}` | — | Soft delete |
| POST | `/recalculate` | — | Tính lại spentAmount cho tất cả active budgets |

### Monitor — `/api/monitor`

| Method | Path | Mô tả |
|---|---|---|
| GET | `/overview` | Giao dịch hôm nay / tuần / tháng (count + income + expense) |
| GET | `/recent` | 50 giao dịch gần nhất |
| GET | `/statistics` | Thống kê theo category, average, largest transaction |
| GET | `/trend` | Trend theo ngày (`?days=7`) |

### Health — `/api/health`

```
GET /api/health   →  200 OK  (không cần auth)
```

---

## Biến môi trường

File `application.yaml` dùng placeholder `${VAR:default_value}`. Khi deploy, truyền qua environment.

| Biến | Mô tả | Default (local) |
|---|---|---|
| `DB_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://localhost:5432/expense_db` |
| `DB_USERNAME` | DB user | `postgres` |
| `DB_PASSWORD` | DB password | `123456` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PASSWORD` | Redis password | _(trống)_ |
| `JWT_SECRET` | Secret ký JWT (Base64, >= 256 bit) | _(hardcoded dev value)_ |
| `JWT_EXPIRATION_MS` | Token TTL milliseconds | `86400000` (24h) |
| `MAIL_USERNAME` | Gmail address | — |
| `MAIL_PASSWORD` | Gmail App Password | — |
| `MAIL_FROM` | From address trong email | — |

> **Prod**: Không bao giờ commit file `.env.prod`. Generate JWT_SECRET bằng: `openssl rand -base64 64`

---

## Deploy

### Môi trường

| Môi trường | Spring Profile | Docker Compose file | DB |
|---|---|---|---|
| Local dev | _(none)_ | `docker-compose.yml` | `expense_db` |
| UAT | `uat` | `docker-compose.uat.yml` | `expense_uat_db` |
| Production | `prod` | `docker-compose.prod.yml` | `expense_prod_db` |

### Build Docker images

```bash
# Backend
docker build -t expense-manager-backend:uat .
docker build -t expense-manager-backend:prod .

# Frontend
docker build -t expense-manager-frontend:uat ./frontend
docker build -t expense-manager-frontend:prod ./frontend
```

### Deploy UAT

```bash
# 1. Copy và điền thông tin thực
cp .env.uat.example .env.uat
vim .env.uat

# 2. Deploy
docker-compose -f docker-compose.uat.yml --env-file .env.uat up -d

# 3. Xem logs
docker-compose -f docker-compose.uat.yml logs -f backend
```

### Deploy Production

```bash
cp .env.prod.example .env.prod
vim .env.prod   # Điền production credentials

docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

### Kiểm tra sau deploy

```bash
# Health check
curl http://your-server/api/health

# Xem logs
docker-compose -f docker-compose.prod.yml logs backend --tail=100
```

### Database migration

Flyway tự chạy khi app khởi động. Trên môi trường có DB mới (chưa có bảng), migration chạy tuần tự từ V1.

> Nếu DB đã tồn tại từ phiên bản cũ (dùng `ddl-auto: update`), cần drop và tạo lại DB trước khi deploy lần đầu với Flyway:
> ```sql
> DROP DATABASE expense_uat_db;
> CREATE DATABASE expense_uat_db;
> ```

---

## Lưu ý khi phát triển

### Thêm tính năng mới — pattern chuẩn

Mỗi domain mới cần tạo đủ các lớp theo thứ tự:

```
Entity  →  Repository  →  DTO (request + response)  →  Mapper  →  Service interface  →  ServiceImpl  →  Controller
```

### Flyway migration

Mỗi thay đổi schema **phải** tạo file migration mới, không sửa file cũ đã apply:
```
V3__Ten_mieu_ta.sql
V4__Ten_mieu_ta.sql
```

### Cache invalidation

Khi tạo/sửa/xóa Transaction, cache phải bị xóa:
```java
cacheService.evictPattern("dashboard:" + userId);
cacheService.evictPattern("statistics:" + userId);
```

### Soft delete

Không dùng `DELETE` SQL cho entity có `is_deleted`. Luôn set `isDeleted = true` và `repository.save()`.

### Transaction status

- Chỉ **PENDING** transaction mới có thể bị confirm/cancel/edit
- Xóa transaction **CONFIRMED** sẽ hoàn lại balance của MoneySource
- Budget spending chỉ tính transaction **CONFIRMED**

### Lombok và boolean fields

Với `boolean isXxx` (primitive), Lombok tạo setter là `setXxx()` (không có "is"). Ví dụ:
```java
boolean isDeleted → setDeleted(true)   // ĐÚNG
boolean isDeleted → setIsDeleted(true)  // SAI
```

### Spring Security 7 (Spring Boot 4.x)

`DaoAuthenticationProvider` yêu cầu `UserDetailsService` qua constructor:
```java
new DaoAuthenticationProvider(userDetailsService)  // ĐÚNG (Spring Security 7)
new DaoAuthenticationProvider()                    // SAI (Spring Security 6 trở về)
```

### Frontend — API service

Tất cả gọi API qua `frontend/src/services/api.js`. Axios interceptor tự gắn JWT header và redirect về `/login` khi nhận 401.
