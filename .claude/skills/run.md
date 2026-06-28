---
description: Chạy backend Spring Boot trong local development mode
---

Chạy backend trong môi trường development:

1. Trước hết, khởi động infrastructure (nếu chưa chạy):
```bash
docker-compose up -d postgres redis
```

2. Chạy backend:
```bash
./mvnw spring-boot:run
```

Backend sẽ chạy tại `http://localhost:8080`.
Flyway tự động chạy migration khi khởi động.

Để chạy với UAT profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=uat
```
