---
description: Template prompt để tạo service + controller + mapper mới
---

Tuân theo project conventions khi tạo feature mới:

## 1. Entity (nếu cần bảng mới)
- Đặt tại `entity/`, extends base fields (id, createdAt, updatedAt)
- Soft-delete: thêm `isDeleted` boolean

## 2. Repository (Spring Data JPA)
- Interface tại `repository/`
- extends `JpaRepository<Entity, String>`
- Method naming: `findByUserIdAndIsDeletedFalse` pattern
- `@Repository` annotation

## 3. Mapper
- `@Component` class tại `mapper/`
- Method: `Entity toResponse(Entity entity)` và `Entity toEntity(RequestDTO dto)`
- Không dùng MapStruct

## 4. Service (interface + impl)
- Interface tại `service/` (không `Impl` suffix)
- Implementation tại `service/impl/` {Entity}ServiceImpl
- `@Slf4j`, `@RequiredArgsConstructor`, `@Transactional`
- Read methods: `@Transactional(readOnly = true)`
- Business logic kiểm tra sở hữu (userId match)

## 5. Controller
- `@RestController`, `@RequiredArgsConstructor`
- `@RequestMapping("/api/v1/{resource}")`
- Inject `@AuthenticationPrincipal UserPrincipal principal` để lấy `userId`
- Return `ResponseEntity<ApiResponse<T>>`

## 6. Migration (nếu cần)
- Tạo Flyway migration file

## 7. Test
- Unit test cho service layer
- Integration test cho controller
