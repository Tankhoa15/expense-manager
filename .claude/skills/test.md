---
description: Chạy tests cho backend
---

## Test

### Chạy tất cả tests
```bash
./mvnw test
```

### Chạy 1 class test cụ thể
```bash
./mvnw test -Dtest=ExpenseManagerApplicationTests
```

### Chạy test kèm build
```bash
./mvnw clean verify
```

## Lưu ý

- Test dùng H2 in-memory DB (PostgreSQL compatibility mode).
- Flyway bị disable trong test profile (`spring.flyway.enabled=false`).
- JPA `ddl-auto=create-drop` trong test profile.
- Hiện tại mới chỉ có 1 test class context-loads.
- Test config: `src/test/resources/application-test.yaml`.
