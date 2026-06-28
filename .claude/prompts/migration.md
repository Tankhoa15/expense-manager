---
description: Hướng dẫn tạo Flyway migration mới
---

Khi cần thay đổi database schema, làm theo các bước sau:

1. Tạo file mới tại `src/main/resources/db/migration/V{next_version}__{Description}.sql`
   - Convention đặt tên: `V{number}__{Short_Description}.sql`
   - Ví dụ: `V4__Add_Color_To_Categories.sql`
2. Viết SQL migration:
   - `CREATE TABLE IF NOT EXISTS`, `ALTER TABLE ADD COLUMN IF NOT EXISTS`
   - Có `UP` và cần `DOWN` không? (Flyway không hỗ trợ `down` mặc định — nếu cần rollback, tạo migration mới)
3. Thêm test nếu có thay đổi schema quan trọng
4. Chạy `./mvnw flyway:info` để verify version mới xuất hiện
5. Spring Boot tự động chạy migration khi `spring-boot:run`

### Template
```sql
-- V4__Add_Color_To_Categories.sql
ALTER TABLE categories
ADD COLUMN IF NOT EXISTS color VARCHAR(7) DEFAULT '#6B7280';

-- Update existing rows
UPDATE categories SET color = '#6B7280' WHERE color IS NULL;
```
