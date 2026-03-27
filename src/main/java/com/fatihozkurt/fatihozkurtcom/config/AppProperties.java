package com.fatihozkurt.fatihozkurtcom.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Aggregates custom application properties under the {@code app} namespace.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Locale locale = new Locale();
    private Jwt jwt = new Jwt();
    private Security security = new Security();
    private Admin admin = new Admin();
    private Mail mail = new Mail();
    private Docs docs = new Docs();
    private Storage storage = new Storage();

    /**
     * Locale selection settings.
     */
    @Getter
    @Setter
    public static class Locale {
        private String defaultLocale = "en";
        private String headerName = "X-Locale";
    }

    /**
     * JWT settings for access and refresh flow.
     */
    @Getter
    @Setter
    public static class Jwt {
        private String issuer;
        private long accessExpirationMinutes;
        private long refreshExpirationDays;
        private String secret;
    }

    /**
     * Security and rate-limit related settings.
     */
    @Getter
    @Setter
    public static class Security {
        private Cors cors = new Cors();
        private boolean requireHttps = false;
        private Cookies cookies = new Cookies();
        private RateLimit rateLimit = new RateLimit();
    }

    /**
     * Cookie related security settings.
     */
    @Getter
    @Setter
    public static class Cookies {
        private boolean secure = false;
        private String sameSite = "Lax";
        private String refreshCookieName = "refresh_token";
    }

    /**
     * CORS settings.
     */
    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:4173", "http://localhost:5173");
    }

    /**
     * Endpoint specific rate-limit policies.
     */
    @Getter
    @Setter
    public static class RateLimit {
        private Policy login = new Policy();
        private Policy forgotPassword = new Policy();
        private Policy contact = new Policy();
    }

    /**
     * Single policy settings.
     */
    @Getter
    @Setter
    public static class Policy {
        private int maxAttempts = 5;
        private int windowSeconds = 600;
    }

    /**
     * Admin bootstrap user settings.
     */
    @Getter
    @Setter
    public static class Admin {
        private Bootstrap bootstrap = new Bootstrap();
    }

    /**
     * Bootstrap user details.
     */
    @Getter
    @Setter
    public static class Bootstrap {
        private boolean enabled = false;
        private String username;
        private String password;
        private String email;
    }

    /**
     * Mail provider settings.
     */
    @Getter
    @Setter
    public static class Mail {
        private String from;
        private String provider = "log";
    }

    /**
     * Runtime docs exposure settings.
     */
    @Getter
    @Setter
    public static class Docs {
        private boolean enabled = false;
    }

    /**
     * Object storage settings.
     */
    @Getter
    @Setter
    public static class Storage {
        private boolean enabled = false;
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String region = "us-east-1";
        private Buckets buckets = new Buckets();
    }

    /**
     * Bucket names grouped by asset concern.
     */
    @Getter
    @Setter
    public static class Buckets {
        private String publicAssets = "public-assets";
        private String projectAssets = "project-assets";
        private String resumeAssets = "resume-assets";
    }
}
