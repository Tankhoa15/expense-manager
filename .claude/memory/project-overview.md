---
name: project-overview
description: Tổng quan về dự án Expense Manager
metadata:
  type: project
  domain: general
---

# Expense Manager

Full-stack personal expense management application.

**Tech stack**: Spring Boot 4.0.4 + Java 21 + PostgreSQL 16 + Redis 7 + React 18 + Vite 5 + Tailwind CSS 3

**Architecture**: 5-layer (Entity → Repository → Service → Controller → API). Services follow interface + impl pattern. All responses wrapped in `ApiResponse<T>`.

**Auth**: JWT (jjwt 0.12.5), stateless sessions.

**DB**: Flyway migrations (V1→V3), JPA `ddl-auto=validate`.

**Key business domains**: User, Category, MoneySource, Transaction (PENDING→CONFIRMED/CANCELLED lifecycle), Budget (DAILY/WEEKLY/MONTHLY/YEARLY periods), MonthlyBalance, Dashboard (aggregated), Monitor (cached via Redis).

**Why**: Track personal expenses with full CRUD, budget alerts via email, dashboard and statistics.

**How to apply**: Dùng làm reference khi thêm feature mới — follow đúng layered architecture, soft-delete pattern, transaction lifecycle, và conventions trong CLAUDE.md.
