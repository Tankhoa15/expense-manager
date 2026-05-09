package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.entity.UserSession;
import com.dev.expense_manager.repository.UserSessionRepository;
import com.dev.expense_manager.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository sessionRepository;

    @Override
    @Transactional
    public void createSession(UserSession session) {
        sessionRepository.save(session);
        log.info("Session created for user {}", session.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    @Override
    @Transactional
    public void invalidateSession(String sessionId) {
        sessionRepository.invalidateSession(sessionId);
        log.info("Session {} invalidated", sessionId);
    }

    @Override
    @Transactional
    public void invalidateAllUserSessions(String userId) {
        sessionRepository.invalidateAllUserSessions(userId);
        log.info("All sessions invalidated for user {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> getUserSessions(String userId) {
        return sessionRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        log.info("Expired sessions cleaned up");
    }
}
