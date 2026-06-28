---
description: Build project cho các môi trường
---

## Build

### Build backend (cả project)
```bash
./mvnw clean package
# File JAR ở target/expense-manager-*.jar
```

### Build backend + chạy test
```bash
./mvnw clean verify
```

### Build backend, skip test
```bash
./mvnw clean package -DskipTests
```

### Build frontend
```bash
cd frontend && npm run build
# Output ở frontend/dist/
```

### Build Docker images
```bash
# Backend
docker build -t expense-manager-backend .

# Frontend
docker build -t expense-manager-frontend frontend/

# Chạy toàn bộ
docker-compose up -d
```

### Kiểm tra compile (không tạo JAR)
```bash
./mvnw compile
```
