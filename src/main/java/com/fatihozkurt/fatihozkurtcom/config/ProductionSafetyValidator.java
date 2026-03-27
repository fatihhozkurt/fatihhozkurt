package com.fatihozkurt.fatihozkurtcom.config;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Validates critical security settings before booting in production profile.
 */
@Component
@RequiredArgsConstructor
public class ProductionSafetyValidator {

    private static final String STRONG_PASSWORD_REGEX = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\d]).{8,}$";

    private final Environment environment;
    private final AppProperties appProperties;

    /**
     * Performs fail-fast checks for production deployments.
     */
    @PostConstruct
    public void validateProductionSafety() {
        boolean productProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch("product"::equalsIgnoreCase);
        if (!productProfile) {
            return;
        }

        String jwtSecret = appProperties.getJwt().getSecret();
        if (jwtSecret == null || jwtSecret.length() < 48 || jwtSecret.toLowerCase(Locale.ROOT).contains("replace")) {
            throw new IllegalStateException("JWT_SECRET is weak for product profile. Use at least 48 random characters.");
        }

        if (!appProperties.getSecurity().getCookies().isSecure()) {
            throw new IllegalStateException("Secure cookies must be enabled for product profile.");
        }

        if (appProperties.getAdmin().getBootstrap().isEnabled()
                && (appProperties.getAdmin().getBootstrap().getPassword() == null
                || !appProperties.getAdmin().getBootstrap().getPassword().matches(STRONG_PASSWORD_REGEX))) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD must satisfy strong password policy in product profile.");
        }

        if (appProperties.getAdmin().getBootstrap().isEnabled()
                && (isBlank(appProperties.getAdmin().getBootstrap().getUsername())
                || isBlank(appProperties.getAdmin().getBootstrap().getEmail()))) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_USERNAME and ADMIN_BOOTSTRAP_EMAIL must be provided when bootstrap is enabled.");
        }

        boolean hasLocalhostOrigin = appProperties.getSecurity().getCors().getAllowedOrigins().stream()
                .anyMatch(origin -> origin.toLowerCase(Locale.ROOT).contains("localhost"));
        if (hasLocalhostOrigin) {
            throw new IllegalStateException("APP_CORS_ALLOWED_ORIGINS must not include localhost in product profile.");
        }

        if (!appProperties.getStorage().isEnabled()) {
            throw new IllegalStateException("Object storage must be enabled for product profile.");
        }

        String storageEndpoint = appProperties.getStorage().getEndpoint();
        if (storageEndpoint == null || storageEndpoint.isBlank()) {
            throw new IllegalStateException("APP_STORAGE_ENDPOINT must be defined for product profile.");
        }

        String storageAccessKey = appProperties.getStorage().getAccessKey();
        String storageSecretKey = appProperties.getStorage().getSecretKey();
        if (storageAccessKey == null || storageSecretKey == null
                || storageAccessKey.equalsIgnoreCase("minioadmin")
                || storageSecretKey.equalsIgnoreCase("minioadmin")) {
            throw new IllegalStateException("Storage credentials must be rotated for product profile.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
