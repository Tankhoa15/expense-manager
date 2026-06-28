---
description: Code review checklist — kiểm tra code thay đổi có đúng convention không
---

Yêu cầu: Review các file đã thay đổi trong working tree, kiểm tra:

## Backend Checklist

1. **Soft delete**: Repository methods có filter `is_deleted = false` không?
2. **Error handling**: Đã throw đúng exception type (`ResourceNotFoundException`, `BadRequestException`, `DuplicateResourceException`) chưa?
3. **Transactional**: Các service method thay đổi dữ liệu có `@Transactional` chưa?
   - Pure-read methods: `@Transactional(readOnly = true)`
4. **Cache eviction**: Transaction thay đổi → gọi `cacheService.evictPattern()` để invalidate dashboard/statistics chưa?
5. **Ownership check**: Đã kiểm tra record thuộc về user hiện tại chưa? (so sánh `entity.user.id` với `userId`)
6. **ApiResponse**: Controller trả về `ResponseEntity<ApiResponse<T>>` chưa?
7. **Validation**: Request DTO có dùng `@Valid` + jakarta validation annotations không?
8. **Naming**: Snaken SQL ↔ camelCase Java consistency
9. **Mapper**: Có mapper class để chuyển Entity → DTO không?

## Frontend Checklist

1. **Route**: Page component đã được thêm vào `App.jsx` chưa?
2. **API path**: Gọi đúng endpoint path (`/api/v1/...`)
3. **Error handling**: Có try/catch cho API calls không?
4. **Auth**: API calls có kèm JWT token không? (Axios interceptor tự động thêm)
