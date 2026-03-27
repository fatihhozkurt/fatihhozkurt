package com.fatihozkurt.fatihozkurtcom.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RateLimitService}.
 */
class RateLimitServiceTest {

    @SuppressWarnings("unchecked")
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getSecurity().getRateLimit().getLogin().setMaxAttempts(1);
        appProperties.getSecurity().getRateLimit().getLogin().setWindowSeconds(60);
        appProperties.getSecurity().getRateLimit().getForgotPassword().setMaxAttempts(1);
        appProperties.getSecurity().getRateLimit().getForgotPassword().setWindowSeconds(60);
        appProperties.getSecurity().getRateLimit().getContact().setMaxAttempts(2);
        appProperties.getSecurity().getRateLimit().getContact().setWindowSeconds(60);
        ObjectProvider<StringRedisTemplate> redisTemplateProvider = (ObjectProvider<StringRedisTemplate>) org.mockito.Mockito.mock(ObjectProvider.class);
        org.mockito.Mockito.when(redisTemplateProvider.getIfAvailable()).thenReturn(null);
        rateLimitService = new RateLimitService(redisTemplateProvider, appProperties);
    }

    @Test
    void checkLoginShouldAllowWithinLimit() {
        assertThatCode(() -> rateLimitService.checkLogin("10.0.0.1:user"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkLoginShouldThrowSec001AfterLimitExceeded() {
        rateLimitService.checkLogin("10.0.0.2:user");

        assertThatThrownBy(() -> rateLimitService.checkLogin("10.0.0.2:user"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SEC001);
    }

    @Test
    void checkForgotPasswordShouldUseIndependentCounter() {
        rateLimitService.checkLogin("10.0.0.3:user");

        assertThatCode(() -> rateLimitService.checkForgotPassword("10.0.0.3:user@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkContactShouldAllowConfiguredAttemptCount() {
        rateLimitService.checkContact("10.0.0.4:user@example.com");
        rateLimitService.checkContact("10.0.0.4:user@example.com");

        assertThatThrownBy(() -> rateLimitService.checkContact("10.0.0.4:user@example.com"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SEC001);
    }
}
