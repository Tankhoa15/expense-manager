Expense Manager - Ứng dụng quản lý chi tiêu cá nhân với kiến trúc fullstack:
- Backend: Spring Boot 4.0.4 + Java 21 + PostgreSQL 16 + Redis 7
- Frontend: React + Vite + Tailwind CSS
- 83 file Java, code compile thành công ✅

  ---
✅ CHỨC NĂNG HOÀN CHỈNH

1. Authentication & Authorization

- ✅  JWT-based authentication (jjwt 0.12.5)
- ✅  Register với auto-create default categories + ví Cash
- ✅  Login trả về accessToken
- ✅  Security filter chain với stateless session
- ✅  Password encryption với BCrypt
- ⚠️ Lưu ý: Chỉ hỗ trợ local auth (email/password), chưa có OAuth2 provider nào được cấu hình

2. Transaction Management (Core Feature)

- ✅  3 trạng thái: PENDING → CONFIRMED → CANCELLED
- ✅  CRUD transaction với soft delete
- ✅  Filter: all, pending, date-range
- ✅  Pagination support (default 20 items)
- ✅  Auto update balance khi confirm/delete transaction
- ✅  Cache invalidation sau mỗi thay đổi

Transaction Flow:
CREATE (PENDING) → CONFIRM → Update MoneySource balance
↓ CANCEL
(không ảnh hưởng balance)

3. Money Sources (Quản lý Ví)

- ✅  5 loại: CASH, BANK_ACCOUNT, CREDIT_CARD, E_WALLET, OTHER
- ✅  Track currentBalance tự động
- ✅  CRUD với soft delete
- ✅  Khi update initialBalance, currentBalance được điều chỉnh theo

4. Categories

- ✅  Phân loại INCOME/EXPENSE
- ✅  Custom icon + color
- ✅  Default categories tự tạo khi register
- ✅  Seed endpoint để tạo lại default
- ✅ Soft delete

5. Budget Management

- ✅  4 kỳ: DAILY, WEEKLY, MONTHLY, YEARLY
- ✅  Track theo category hoặc overall (categoryId null)
- ✅  Auto-calculate spentAmount realtime
- ✅  Alert threshold với email notification
- ✅  Recalculate endpoint
- ⚠️ Chỉ tính transaction CONFIRMED

6. Monthly Balance

- ✅  Opening balance theo tháng
- ✅  Auto calculate totalIncome/totalExpense
- ✅  Current month endpoint

7. Dashboard

- ✅  Summary: totalBalance, monthIncome, monthExpense, pendingAmount
- ✅  Money sources overview
- ✅ Recent transactions
- ✅  Category breakdown
- ✅  Redis cache 5 phút

8. Monitor/Statistics

- ✅  Overview: today/week/month statistics
- ✅  Recent 50 transactions
- ✅  Statistics by category
- ✅  Trend analysis (configurable days)
- ✅  Redis cache 10 phút

9. Email Notifications

- ✅  Budget alert khi vượt threshold
- ✅  Async sending với @Async
- ✅  Gmail SMTP integration

10. Health Check

- ✅  /api/health endpoint (public, không cần auth)

  ---
🏗 KIẾN TRÚC & CODE QUALITY

✅ Điểm Mạnh

1. Layered Architecture chuẩn:

2. Database Migration với Flyway:
   - V1: Initial schema
   - V2: Add money_sources + transaction_status
   - V3: Drop unused 2FA features
   - ✅  DDL auto = validate (không tự sửa schema)
3. Security Best Practices:
   - Stateless JWT
   - Password BCrypt
   - CORS configuration
   - Exception handling toàn cục
   - UserPrincipal injection vào controller
4. Transaction Management:
   - @Transactional đúng chỗ
   - Soft delete pattern nhất quán
   - Optimistic locking với version (nếu có)
5. Caching Strategy:
   - Redis cho dashboard + statistics
   - Pattern-based eviction
   - TTL riêng cho từng loại cache
   - Multi-stage build
   - Health checks
   - Resource limits
   - Separate compose files cho local/uat/prod