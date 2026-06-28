# Backend Architecture — Expense Manager

Tài liệu mô tả kiến trúc backend theo luồng **Database → Entity → Repository → Service → Controller (API)**.

> Backend được build từ `src/` (Maven `pom.xml` ở root). Mọi domain đều đi qua đủ 5 tầng, không có controller gọi thẳng repository.

---

## 1. Tech stack

| Thành phần | Công nghệ |
|---|---|
| Runtime | Java 21, Spring Boot 4.0.4 |
| Web | `spring-boot-starter-webmvc` (REST) |
| Persistence | Spring Data JPA + Hibernate, PostgreSQL |
| Migration | Flyway (`src/main/resources/db/migration`) |
| Bảo mật | Spring Security + JWT (jjwt 0.12.5), stateless |
| Cache | Redis (chỉ dùng cho các endpoint `/api/monitor`) |
| Email | `spring-boot-starter-mail` (gửi bất đồng bộ qua `@Async`) |
| Boilerplate | Lombok |

Profile cấu hình: `application.yaml` (default/local), `application-uat.yaml`, `application-prod.yaml`. Tất cả giá trị nhạy cảm đọc qua biến môi trường `${VAR:default}`.

---

## 2. Sơ đồ tầng (request flow)

```
HTTP request
   │
   ▼
JwtAuthenticationFilter  ──►  set Authentication (UserPrincipal) vào SecurityContext
   │
   ▼
Controller (@RestController)        ← chỉ điều phối, không có business logic
   │   - đọc userId từ @AuthenticationPrincipal UserPrincipal
   │   - validate body (@Valid)
   │   - bọc kết quả vào ApiResponse<T>
   ▼
Service (interface + Impl)          ← toàn bộ business logic, @Transactional
   │   - kiểm tra quyền sở hữu (record thuộc userId)
   │   - gọi Mapper để map Entity ↔ DTO
   ▼
Repository (Spring Data JPA)        ← truy vấn DB, lọc soft-delete (isDeletedFalse)
   │
   ▼
PostgreSQL
```

**Quy ước tầng:**
- **Controller**: mỏng, không chứa logic nghiệp vụ. Lấy `userId` từ `UserPrincipal`, trả `ApiResponse<T>`.
- **Service**: tách `interface` (trong `service/`) và `Impl` (trong `service/impl/`). Đánh dấu `@Transactional` (read-only cho truy vấn).
- **Mapper** (`mapper/`): chuyển Entity → Response DTO, tránh map thủ công trong service.
- **Repository**: đặt tên method có hậu tố `...IsDeletedFalse` để tôn trọng soft-delete.

---

## 3. Database schema

Migration: `V1__Initial_Schema.sql`, `V2__Add_Money_Sources_And_Transaction_Status.sql`, `V3__Drop_Unused_Features.sql`.

### Enum types (PostgreSQL)
- `transaction_type`: `INCOME`, `EXPENSE`
- `transaction_status`: `PENDING`, `CONFIRMED`, `CANCELLED`
- `budget_period`: `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`
- `money_source_type`: `CASH`, `BANK_ACCOUNT`, `CREDIT_CARD`, `E_WALLET`, `OTHER`

### Bảng & quan hệ

```
users ─┬─< categories ──< transactions >── money_sources
       ├─< budgets >── categories (nullable)
       ├─< money_sources
       └─< monthly_balances
```

| Bảng | Cột chính | Ghi chú |
|---|---|---|
| `users` | id, email (unique), password, full_name, email_verified | `isEnabled()` = `email_verified` |
| `categories` | id, name, icon, color, type, user_id, is_default, **is_deleted** | unique `(user_id, name)` |
| `transactions` | id, amount, type, **status**, transaction_date, user_id, category_id, **money_source_id**, **is_deleted** | category_id `ON DELETE RESTRICT` |
| `budgets` | id, amount, spent_amount, period, period_start/end, category_id (nullable = overall), alert_threshold, is_active, **is_deleted** | |
| `money_sources` | id, name, source_type, initial_balance, current_balance, user_id, is_default, **is_deleted** | |
| `monthly_balances` | id, user_id, year, month, opening_balance | unique `(user_id, year, month)` |

**Soft delete**: `categories`, `transactions`, `budgets`, `money_sources` không xóa vật lý — set `is_deleted = true`. Mọi truy vấn đọc đều lọc `is_deleted = false`.

> `V3` đã **drop** cột 2FA (`two_factor_*`, `backup_codes`) khỏi `users` và **drop** bảng `user_sessions` — đây là phần chưa từng được wiring vào code.

---

## 4. Domain modules (Entity → Repository → Service → API)

Mỗi domain theo đúng chuỗi: **Entity** → **Repository** → **Service (interface + Impl)** → **Controller**.

### 4.1 Auth / User
- **Entity**: `User` (implements `UserDetails`)
- **Repository**: `UserRepository` — `findByEmail`, `existsByEmail`
- **Service**: `UserService` / `UserServiceImpl`
  - `register()` orchestrate: tạo user → seed default categories → tạo money source mặc định (gọi `CategoryService`, `MoneySourceService`).
  - `getCurrentUser()`, `getUserByEmail()`
- **Security**: `JwtTokenProvider` (tạo/parse token), `JwtAuthenticationFilter` (đọc Bearer token mỗi request), `CustomUserDetailsService`, `UserPrincipal`.
- **API** (`/api/auth`):
  | Method | Path | Mô tả |
  |---|---|---|
  | POST | `/register` | Đăng ký, trả `UserResponse` |
  | POST | `/login` | Trả `{ accessToken, tokenType, user }` |
  | GET | `/me` | Thông tin user hiện tại |

### 4.2 Category
- **Entity**: `Category` (type: `TransactionType`, soft-delete)
- **Repository**: `CategoryRepository` — `findByUserIdAndIsDeletedFalse`, `findByUserIdAndTypeAndIsDeletedFalse`, `existsByUserIdAndNameAndIsDeletedFalse`, ...
- **Service**: `CategoryService` / `CategoryServiceImpl` — CRUD + `seedDefaultCategories()` (clone 12 category mặc định khi đăng ký)
- **API** (`/api/categories`): `POST`, `GET`, `GET /{id}`, `GET /type/{type}`, `PUT /{id}`, `DELETE /{id}` (soft delete), `POST /seed`

### 4.3 MoneySource (ví/nguồn tiền)
- **Entity**: `MoneySource` (`MoneySourceType`, `initialBalance` / `currentBalance`)
- **Repository**: `MoneySourceRepository` — `findByUserIdAndIsDeletedFalse`, `sumCurrentBalanceByUserId`, ...
- **Service**: `MoneySourceService` / `MoneySourceServiceImpl` — CRUD; khi đổi `initialBalance` thì điều chỉnh `currentBalance` tương ứng; `createDefaultSource()` cho user mới
- **API** (`/api/money-sources`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

### 4.4 Transaction
- **Entity**: `Transaction` (type, **status**, liên kết Category bắt buộc + MoneySource tùy chọn, soft-delete)
- **Repository**: `TransactionRepository`
  - `sumAmountByUserIdAndTypeAndDateRange` (chỉ tính `status = CONFIRMED`)
  - `sumAmountByCategoryAndTypeAndDateRange`, `sumAmountByUserIdAndCategoryIdAndDateRange`, `sumPendingAmountByUserId`
  - các method phân trang `...IsDeletedFalseOrderBy...`
- **Service**: `TransactionService` / `TransactionServiceImpl`
  - Vòng đời status: tạo ở `PENDING` → `confirm()` (cộng/trừ `currentBalance` của MoneySource theo INCOME/EXPENSE) → hoặc `cancel()`.
  - `update()` chỉ cho phép khi chưa `CONFIRMED`.
  - `delete()` (soft) đảo lại số dư nếu giao dịch đã `CONFIRMED`.
  - Sau mỗi thay đổi: `evictCache()` xóa cache dashboard/statistics.
- **API** (`/api/transactions`):
  | Method | Path | Mô tả |
  |---|---|---|
  | POST | `/` | Tạo (status `PENDING`) |
  | GET | `/` | Danh sách (phân trang) |
  | GET | `/pending` | Giao dịch chờ xác nhận |
  | GET | `/{id}` | Chi tiết |
  | GET | `/date-range` | Lọc theo khoảng ngày |
  | PUT | `/{id}` | Sửa (khi chưa confirmed) |
  | POST | `/{id}/confirm` | Xác nhận → cập nhật số dư |
  | POST | `/{id}/cancel` | Hủy |
  | DELETE | `/{id}` | Xóa mềm |

### 4.5 Budget
- **Entity**: `Budget` (`BudgetPeriod`, `alertThreshold`, helper `getRemainingAmount()` / `getPercentageUsed()`)
- **Repository**: `BudgetRepository` — tìm budget active theo category/period/date, phát hiện overlap
- **Service**: `BudgetService` / `BudgetServiceImpl`
  - Tính lại `spentAmount` từ transactions; khi vượt `alertThreshold` (và lần trước chưa vượt) → gọi `EmailService.sendBudgetAlertEmail()` (bất đồng bộ).
- **API** (`/api/budgets`): `POST`, `GET`, `GET /{id}`, `GET /alerts`, `PUT /{id}`, `DELETE /{id}`, `POST /recalculate`

### 4.6 MonthlyBalance
- **Entity**: `MonthlyBalance` (year, month, opening_balance)
- **Repository**: `MonthlyBalanceRepository` — `findByUserIdAndYearAndMonth`, `findByUserIdOrderByYearDescMonthDesc`
- **Service**: `MonthlyBalanceService` / `MonthlyBalanceServiceImpl` — lưu `opening_balance`, tính income/expense của tháng on-the-fly từ transactions
- **API** (`/api/monthly-balances`): `POST` (create or update), `GET`, `GET /current`

### 4.7 Dashboard (read-model tổng hợp)
- **Service**: `DashboardService` / `DashboardServiceImpl` — gom từ nhiều repository (MoneySource, Transaction, Category) trả `DashboardSummaryResponse`: `totalBalance`, `monthIncome`, `monthExpense`, `pendingAmount`, `moneySources[]`, `recentTransactions[]`, `categoryBreakdown[]`
- **API** (`/api/dashboard`): `GET`

### 4.8 Transaction Monitor (có cache)
- **Service**: `TransactionMonitorService` / `TransactionMonitorServiceImpl` — `getOverview`, `getRecentActivity`, `getStatistics`, `getTransactionTrend`; kết quả được cache qua `CacheService` (Redis), TTL theo `app.cache.*`.
- **API** (`/api/monitor`): `GET /overview`, `GET /recent`, `GET /statistics`, `GET /trend?days=`

### 4.9 Health
- **API** (`/api`): `GET /health`

---

## 5. Cross-cutting concerns

### Security / JWT
- Stateless. `JwtAuthenticationFilter` đọc header `Authorization: Bearer <token>`, verify bằng `JwtTokenProvider`, nạp `UserPrincipal` vào `SecurityContext`.
- Cấu hình tại `SecurityConfig` (`/api/auth/**`, `/api/health` public; còn lại yêu cầu auth).
- `JwtAuthenticationEntryPoint` trả 401 khi chưa xác thực.

### Response envelope
Mọi endpoint trả `ApiResponse<T>` thống nhất: `{ success, message, data }`. Lỗi được chuẩn hóa tại `GlobalExceptionHandler` (`ResourceNotFoundException` → 404, `BadRequestException` → 400, `DuplicateResourceException` → 409, validation → 400).

### Cache (Redis)
- `CacheService` / `CacheServiceImpl` (prefix `expense:cache:`). Chỉ phục vụ các endpoint `/api/monitor`.
- Khi transaction thay đổi, `TransactionServiceImpl.evictCache()` xóa key `dashboard:*` và `statistics:*` của user.

### Email (async)
- `EmailServiceImpl` gửi HTML qua SMTP, các method gắn `@Async` (bật bởi `AsyncConfig @EnableAsync`). Hai loại: welcome email và budget alert.

### Soft delete
- 4 entity nghiệp vụ dùng cờ `is_deleted`. Service set cờ thay vì xóa; repository chỉ đọc bản ghi `is_deleted = false`.

---

## 6. Tổng hợp API

| Prefix | Domain |
|---|---|
| `/api/auth` | Đăng ký / đăng nhập / thông tin user |
| `/api/categories` | Danh mục thu/chi |
| `/api/money-sources` | Nguồn tiền / ví |
| `/api/transactions` | Giao dịch (PENDING → CONFIRMED/CANCELLED) |
| `/api/budgets` | Ngân sách + cảnh báo |
| `/api/monthly-balances` | Số dư đầu kỳ theo tháng |
| `/api/dashboard` | Tổng quan dashboard |
| `/api/monitor` | Thống kê (có cache Redis) |
| `/api/health` | Health check |
