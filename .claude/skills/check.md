---
description: Kiểm tra code style và convention trước khi commit
---

Kiểm tra codebase xem có vi phạm convention không:

1. **Compile check**: `./mvnw compile` — đảm bảo code compile được
2. **Code style**:
   - Controllers không có business logic
   - Services đều có interface trong `service/` và impl trong `service/impl/`
   - Repository methods có hậu tố `IsDeletedFalse` cho soft-delete entities
   - Mọi API response đều dùng `ApiResponse<T>`
   - `@Transactional` trên service methods
   - UserPrincipal injection đúng cách (`@AuthenticationPrincipal UserPrincipal`)
3. **Frontend**: kiểm tra không dùng import/export sai path, kiểm tra route/page mapping
4. **Migration**: Flyway files đúng naming convention `V{version}__{Description}.sql`
