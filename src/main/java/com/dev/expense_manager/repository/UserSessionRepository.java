package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    List<UserSession> findByUserIdAndIsActiveTrue(String userId);

    Optional<UserSession> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    void invalidateAllUserSessions(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.id = :sessionId")
    void invalidateSession(@Param("sessionId") String sessionId);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.lastAccessedAt = :now WHERE s.id = :sessionId")
    void updateLastAccessed(@Param("sessionId") String sessionId, @Param("now") LocalDateTime now);
}
