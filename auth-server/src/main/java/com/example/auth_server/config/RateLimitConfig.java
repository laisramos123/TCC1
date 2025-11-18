package com.example.auth_server.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }
}

@Component
@Order(1)
@Slf4j
class RateLimitFilter extends OncePerRequestFilter {

    @Value("${rate.limit.requests.per.minute:60}")
    private int requestsPerMinute;

    @Value("${rate.limit.burst.capacity:100}")
    private int burstCapacity;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for health checks
        String path = request.getRequestURI();
        if (path.contains("/actuator/health") || path.contains("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get client identifier (IP address or API key)
        String clientId = getClientIdentifier(request);

        // Get or create bucket for this client
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(bucket.getAvailableTokens()));
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(requestsPerMinute));

            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            handleRateLimitExceeded(response, bucket);
        }
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get API key first
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey;
        }

        // Try to get authenticated user
        if (request.getUserPrincipal() != null) {
            return "user:" + request.getUserPrincipal().getName();
        }

        // Fall back to IP address
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        return "ip:" + clientIp;
    }

    private Bucket createBucket(String clientId) {
        // Different limits for different client types
        Bandwidth limit;

        if (clientId.startsWith("api:")) {
            // Higher limit for API keys
            limit = Bandwidth.classic(burstCapacity,
                    Refill.intervally(burstCapacity, Duration.ofMinutes(1)));
        } else if (clientId.startsWith("user:")) {
            // Standard limit for authenticated users
            limit = Bandwidth.classic(requestsPerMinute,
                    Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        } else {

            limit = Bandwidth.classic(requestsPerMinute / 2,
                    Refill.intervally(requestsPerMinute / 2, Duration.ofMinutes(1)));
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void handleRateLimitExceeded(HttpServletResponse response, Bucket bucket)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.addHeader("X-Rate-Limit-Remaining", "0");
        response.addHeader("X-Rate-Limit-Retry-After", "60");

        String errorMessage = """
                {
                "error": "rate_limit_exceeded",
                "error_description": "Too many requests. Please try again later.",
                "retry_after": 60
                }
                """;

        response.getWriter().write(errorMessage);

        log.warn("Rate limit exceeded for client");
    }

    /**
     * Clean up old buckets periodically to prevent memory leak
     */
    @jakarta.annotation.PostConstruct
    public void startCleanupTask() {
        // Schedule cleanup every hour
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> {
                    int sizeBefore = buckets.size();
                    // Remove buckets that haven't been used recently
                    buckets.entrySet().removeIf(entry -> entry.getValue().getAvailableTokens() == requestsPerMinute);
                    int sizeAfter = buckets.size();
                    if (sizeBefore != sizeAfter) {
                        log.debug("Cleaned up {} unused rate limit buckets", sizeBefore - sizeAfter);
                    }
                }, 1, 1, java.util.concurrent.TimeUnit.HOURS);
    }
}