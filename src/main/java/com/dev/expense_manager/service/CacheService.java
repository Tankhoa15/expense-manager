package com.dev.expense_manager.service;

import java.util.concurrent.Callable;

public interface CacheService {
    <T> T get(String key, Class<T> type);
    <T> T get(String key, Class<T> type, Callable<T> loader);
    void put(String key, Object value);
    void evict(String key);
    void evictPattern(String pattern);
}
