package com.fatihozkurt.fatihozkurtcom.security.ratelimit;

import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Provides in-memory rate limiting for abuse-prone endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate-limit:";

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final AppProperties appProperties;

    /**
     * Verifies login policy for given key.
     *
     * @param key ip or ip+username key
     */
    public void checkLogin(String key) {
        check("login:" + key, appProperties.getSecurity().getRateLimit().getLogin());
    }

    /**
     * Verifies forgot-password policy for given key.
     *
     * @param key ip or ip+email key
     */
    public void checkForgotPassword(String key) {
        check("forgot:" + key, appProperties.getSecurity().getRateLimit().getForgotPassword());
    }

    /**
     * Verifies contact policy for given key.
     *
     * @param key ip or ip+email key
     */
    public void checkContact(String key) {
        check("contact:" + key, appProperties.getSecurity().getRateLimit().getContact());
    }

    /**
     * Clears in-memory and Redis counters.
     *
     * <p>Used by integration tests to isolate scenarios.</p>
     */
    public void resetCounters() {
        counters.clear();
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        try {
            Set<String> keys = redisTemplate.keys(RATE_LIMIT_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (RuntimeException exception) {
            log.warn("Skipping redis counter cleanup because redis is not reachable reason={}", exception.getMessage());
        }
    }

    private void check(String key, AppProperties.Policy policy) {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                checkWithRedis(redisTemplate, normalizedKey, policy);
                return;
            } catch (AppException appException) {
                throw appException;
            } catch (RuntimeException exception) {
                log.error("Redis-based rate limit failed key={} reason={}", normalizedKey, exception.getMessage());
            }
        }
        checkWithInMemoryCounter(normalizedKey, policy);
    }

    private void checkWithRedis(StringRedisTemplate redisTemplate, String key, AppProperties.Policy policy) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        Long current = redisTemplate.opsForValue().increment(redisKey);
        if (current == null) {
            throw new IllegalStateException("Rate limit counter increment returned null");
        }
        if (current == 1L) {
            redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(policy.getWindowSeconds()));
        }
        if (current > policy.getMaxAttempts()) {
            log.warn("Rate limit exceeded key={} max={} window={}s", key, policy.getMaxAttempts(), policy.getWindowSeconds());
            throw new AppException(ErrorCode.SEC001);
        }
    }

    private void checkWithInMemoryCounter(String key, AppProperties.Policy policy) {
        long now = Instant.now().getEpochSecond();
        WindowCounter counter = counters.computeIfAbsent(key, unused -> new WindowCounter(now, new AtomicInteger(0)));
        synchronized (counter) {
            if (now - counter.windowStart() >= policy.getWindowSeconds()) {
                counter.count().set(0);
                counter.windowStart(now);
            }
            int current = counter.count().incrementAndGet();
            if (current > policy.getMaxAttempts()) {
                log.warn("Rate limit exceeded key={} max={} window={}s", key, policy.getMaxAttempts(), policy.getWindowSeconds());
                throw new AppException(ErrorCode.SEC001);
            }
        }
    }

    private static final class WindowCounter {
        private volatile long windowStart;
        private final AtomicInteger count;

        private WindowCounter(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }

        private long windowStart() {
            return windowStart;
        }

        private void windowStart(long value) {
            this.windowStart = value;
        }

        private AtomicInteger count() {
            return count;
        }
    }
}
