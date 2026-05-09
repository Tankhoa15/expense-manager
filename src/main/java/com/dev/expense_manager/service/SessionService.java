package com.dev.expense_manager.service;

import com.dev.expense_manager.entity.UserSession;
import java.util.List;

public interface SessionService {
    void createSession(UserSession session);
    UserSession getSession(String sessionId);
    void invalidateSession(String sessionId);
    void invalidateAllUserSessions(String userId);
    List<UserSession> getUserSessions(String userId);
    void cleanupExpiredSessions();
}
