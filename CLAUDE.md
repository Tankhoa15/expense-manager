# Expense Manager — CLAUDE.md

Tổng quan dự án và hướng dẫn cho AI assistant làm việc trên codebase này.

## Project Overview

Full-stack personal expense management app.
- **Backend**: Spring Boot 4.0.4 + Java 21 + PostgreSQL 16 + Redis 7
- **Frontend**: React 18 + Vite 5 + Tailwind CSS 3 + Axios
- **Auth**: JWT (jjwt 0.12.5), stateless
- **Build**: Maven Wrapper (`./mvnw`)
- **DB Migration**: Flyway (3 files: V1→V3)
- **Cache**: Redis (dashboard 5min, statistics 10min)

## Architecture

Layered (5 tầng): Entity → Repository → Service (interface + impl) → Controller

```
JwtAuthenticationFilter → Controller → Service → Repository → PostgreSQL
                                       ↘ Mapper (Entity ↔ DTO)
```

- Controllers phải mỏng — không business logic. Lấy `userId` từ `@AuthenticationPrincipal UserPrincipal`.
- Services: interface + impl pattern. `@Transactional` (readOnly cho truy vấn).
- Mappers (`mapper/`): `@Component`, không dùng MapStruct.
- Repositories: query method có hậu tố `IsDeletedFalse` cho soft-delete entities.
- Mọi API response wrapper trong `ApiResponse<T>`: `{ success, message, data, timestamp }`.

## Domain Entities

| Entity | Table | Soft-delete |
|--------|-------|-------------|
| User | users | No |
| Category | categories | Yes |
| Transaction | transactions | Yes (status lifecycle: PENDING→CONFIRMED/CANCELLED) |
| Budget | budgets | Yes |
| MoneySource | money_sources | Yes |
| MonthlyBalance | monthly_balances | No |

Transaction status lifecycle:
- `PENDING` → `confirm()` → `CONFIRMED` (cập nhật MoneySource.currentBalance)
- `PENDING` → `cancel()` → `CANCELLED`
- `delete()`: nếu CONFIRMED thì đảo balance, nếu PENDING thì xóa mềm

## Package Structure

```
com.dev.expense_manager/
├── config/         # SecurityConfig, RedisConfig, AsyncConfig, JacksonConfig, HttpRequestLoggingFilter
├── constant/       # App constants
├── controller/     # REST controllers (9 files)
├── dto/request/    # Input DTOs (@Valid)
├── dto/response/   # Output DTOs
├── entity/         # JPA entities + enums
├── exception/      # GlobalExceptionHandler + custom exceptions
├── mapper/         # Entity ↔ DTO mappers
├── repository/     # Spring Data JPA
├── security/       # JWT + filters + UserPrincipal
├── service/        # Interfaces
├── service/impl/   # Implementations
└── validation/     # @ValidPassword custom annotation
```

## API Prefixes

| Prefix | Domain |
|--------|--------|
| `/api/v1/auth` | Auth (register, login, me) |
| `/api/v1/categories` | Categories |
| `/api/v1/money-sources` | Money sources |
| `/api/v1/transactions` | Transactions |
| `/api/v1/budgets` | Budgets |
| `/api/v1/monthly-balances` | Monthly balances |
| `/api/v1/dashboard` | Dashboard (aggregated) |
| `/api/v1/monitor` | Monitor/statistics (cached via Redis) |
| `/api/v1/health` | Health check (public) |

## Known Issues / TODO

1. **Frontend path mismatch**: Frontend Axios `baseURL: '/api'` nhưng backend controllers dùng `/api/v1/...`. Cần fix Vite proxy và nginx để thêm `/v1`.
2. **Category endpoint mismatch**: Frontend gọi `GET /categories?type=...` nhưng backend định nghĩa `GET /categories/type/{type}` (path variable).
3. **Missing route**: `Budgets.jsx` page component tồn tại nhưng chưa được đăng ký route trong `App.jsx`.
4. **No unit tests**: Chỉ có 1 context-loads test. Service logic chưa được test.
5. **Empty packages**: `event/`, `handle/`, `listenner/`, `utils/` — empty, chưa dùng đến.
6. **Redis `keys` command**: `CacheServiceImpl.evictPattern()` dùng `redisTemplate.keys()` có thể gây block Redis trên production.

## Coding Conventions

- **Java 21**: Dùng `var` cho local variables khi type rõ ràng. Dùng records cho DTOs (`request`/`response` packages).
- **Lombok**: `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- **Imports**: Không dùng wildcard imports.
- **SQL Convention**: Snake Case (`user_id`, `created_at`, `is_deleted`). Java Convention: Camel Case (`userId`, `createdAt`, `isDeleted`). Map qua `@Column(name = "...")`.
- **Naming Repository methods**: `findBy...AndIsDeletedFalse...OrderBy...Desc`.
- **Error handling**: Throw `ResourceNotFoundException` (404), `BadRequestException` (400), `DuplicateResourceException` (409). Global handler bắt tất cả.
- **Soft delete**: Chỉ set `isDeleted=true`, không xóa vật lý. Mọi query đọc phải filter `isDeleted=false`.

## How to Run

```bash
# 1. Start infra (PostgreSQL + Redis)
docker-compose up -d postgres redis

# 2. Run backend
./mvnw spring-boot:run

# 3. Run frontend (separate terminal)
cd frontend && npm install && npm run dev
```

Backend: `http://localhost:8080` | Frontend: `http://localhost:5173` | DB: `localhost:5432/expense_db` | Redis: `localhost:6379`
