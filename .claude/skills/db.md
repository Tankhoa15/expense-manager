---
description: Thao tác với database (migrate, repair, info)
---

## Thao tác database

### Xem trạng thái migrations
```bash
./mvnw flyway:info
```

### Chạy migration (tự động chạy khi spring-boot:run)
```bash
./mvnw flyway:migrate
```

### Repair (khi migration bị lỗi/dirty state)
```bash
./mvnw flyway:repair
```

### Bash vào PostgreSQL
```bash
docker exec -it expense-manager-postgres-1 psql -U postgres -d expense_db
```

### Xóa database và chạy lại
```bash
docker-compose down -v && docker-compose up -d postgres redis
# Sau đó run backend để migration tự động chạy
```

## Lưu ý

- JPA `ddl-auto=validate` — schema chỉ thay đổi qua Flyway, không qua JPA.
- Các migration file đặt tại `src/main/resources/db/migration/V*__*.sql`.
- File migration mới phải đặt tên theo format: `V{version}__{Description}.sql` (2 dấu gạch dưới).
