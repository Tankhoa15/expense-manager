package com.dev.expense_manager.security;

import com.dev.expense_manager.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Long> rateLimitCache;
    private final ObjectMapper objectMapper;

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long TIME_WINDOW_MS = 60_000; // 1 minute

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Apply rate limiting only to auth endpoints
        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register")) {
            String clientIp = getClientIp(request);
            String key = path + ":" + clientIp;

            if (isRateLimited(key)) {
                log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, path);
                sendRateLimitError(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String key) {
        long currentTime = System.currentTimeMillis();

        // Clean up old entries
        rateLimitCache.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > TIME_WINDOW_MS * MAX_REQUESTS_PER_MINUTE
        );

        // Count requests in the current time window
        long count = rateLimitCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(key.substring(0, key.lastIndexOf(':'))))
                .filter(entry -> currentTime - entry.getValue() < TIME_WINDOW_MS)
                .count();

        if (count >= MAX_REQUESTS_PER_MINUTE) {
            return true;
        }

        // Add current request
        rateLimitCache.put(key + ":" + currentTime, currentTime);
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Get first IP if multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void sendRateLimitError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Object> apiResponse = ApiResponse.error("Too many requests. Please try again later.");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
