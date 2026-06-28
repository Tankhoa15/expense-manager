---
name: known-issues
description: Các vấn đề đã biết cần fix
metadata:
  type: project
  domain: todos
---

## Known Issues / TODOs

1. **Frontend path mismatch**: Frontend Axios `baseURL: '/api'` nhưng backend controllers dùng `/api/v1/...`. Cần fix Vite proxy và nginx để thêm `/v1`.
2. **Category endpoint mismatch**: Frontend gọi `GET /categories?type=...` nhưng backend định nghĩa `GET /categories/type/{type}` (path variable).
3. **Missing route**: `Budgets.jsx` page component tồn tại nhưng chưa được đăng ký route trong `App.jsx`.
4. **No unit tests**: Chỉ có 1 context-loads test. Service logic (transaction lifecycle, budget recalculation, cache eviction) chưa được test.
5. **Redis `keys` command**: `CacheServiceImpl.evictPattern()` dùng `redisTemplate.keys()` có thể gây block Redis trên production — nên replace bằng `SCAN`.
6. **Seeded categories differ**: V1 SQL seeds 12 defaults under system user, Java code seeds 10 on user registration (missing "Gift" và "Other Expense").

**Why**: Documented during initial codebase analysis. Các issues này chưa được fix.

**How to apply**: Khi làm việc với frontend, lưu ý path mismatches. Khi thêm test, cover service layer business logic.
