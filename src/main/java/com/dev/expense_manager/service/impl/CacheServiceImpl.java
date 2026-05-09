package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.dashboard.ttl-minutes:5}")
    private int dashboardTtlMinutes;

    @Value("${app.cache.statistics.ttl-minutes:10}")
    private int statisticsTtlMinutes;

    private static final String CACHE_PREFIX = "expense:cache:";

    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            String cacheKey = CACHE_PREFIX + key;
            String value = redisTemplate.opsForValue().get(cacheKey);
            if (value != null) {
                log.debug("Cache hit for key: {}", key);
                return objectMapper.readValue(value, type);
            }
            log.debug("Cache miss for key: {}", key);
            return null;
        } catch (Exception e) {
            log.error("Error getting cache for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T get(String key, Class<T> type, Callable<T> loader) {
        T cached = get(key, type);
        if (cached != null) {
            return cached;
        }

        try {
            T value = loader.call();
            if (value != null) {
                put(key, value);
            }
            return value;
        } catch (Exception e) {
            log.error("Error loading cache for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void put(String key, Object value) {
        try {
            String cacheKey = CACHE_PREFIX + key;
            int ttl = getTtlForKey(key);
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, ttl, TimeUnit.MINUTES);
            log.debug("Cached value for key: {} with TTL: {} minutes", key, ttl);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cache value for key {}: {}", key, e.getMessage());
        }
    }

    @Override
    public void evict(String key) {
        String cacheKey = CACHE_PREFIX + key;
        redisTemplate.delete(cacheKey);
        log.debug("Evicted cache for key: {}", key);
    }

    @Override
    public void evictPattern(String pattern) {
        String cachePattern = CACHE_PREFIX + pattern;
        Set<String> keys = redisTemplate.keys(cachePattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Evicted {} cache entries matching pattern: {}", keys.size(), pattern);
        }
    }

    private int getTtlForKey(String key) {
        if (key.startsWith("dashboard")) {
            return dashboardTtlMinutes;
        } else if (key.startsWith("statistics")) {
            return statisticsTtlMinutes;
        }
        return 5;
    }
}
