package com.fatihozkurt.fatihozkurtcom.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminRole;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AdminUserRepository;
import com.fatihozkurt.fatihozkurtcom.security.ratelimit.RateLimitService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * Shared integration-test support for MockMvc based endpoint tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected AdminUserRepository adminUserRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected RateLimitService rateLimitService;

    private final AtomicInteger ipCounter = new AtomicInteger(10);

    /**
     * Truncates all application tables.
     */
    protected void cleanDatabase() {
        List<String> tables = List.of(
                "password_reset_tokens",
                "refresh_token_sessions",
                "mail_delivery_logs",
                "security_events",
                "admin_audit_events",
                "contact_messages",
                "visit_events",
                "tech_stack_items",
                "project_items",
                "medium_articles",
                "cv_documents",
                "contact_profiles",
                "hero_content",
                "about_content",
                "admin_users"
        );
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        rateLimitService.resetCounters();
    }

    /**
     * Seeds one admin user.
     *
     * @param username username
     * @param email email
     * @param rawPassword raw password
     * @return persisted admin user
     */
    protected AdminUser seedAdmin(String username, String email, String rawPassword) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername(username);
        adminUser.setEmail(email);
        adminUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        adminUser.setRole(AdminRole.ROLE_ADMIN);
        adminUser.setActive(true);
        return adminUserRepository.save(adminUser);
    }

    /**
     * Logs in seeded admin and returns access token with cookie.
     *
     * @param username username
     * @param password password
     * @return auth session
     * @throws Exception request error
     */
    protected AuthSession loginAs(String username, String password) throws Exception {
        String payload = objectMapper.writeValueAsString(new LoginRequestBody(username, password));
        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", nextIp())
                        .content(payload))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = body.path("accessToken").asText();
        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String cookie = setCookie == null ? "" : setCookie.split(";", 2)[0];
        return new AuthSession(accessToken, cookie);
    }

    /**
     * Returns bearer header value.
     *
     * @param token token
     * @return header value
     */
    protected String bearer(String token) {
        return "Bearer " + token;
    }

    /**
     * Returns unique test ip value.
     *
     * @return ip
     */
    protected String nextIp() {
        return "10.0.0." + ipCounter.incrementAndGet();
    }

    /**
     * Carries auth session values.
     *
     * @param accessToken access token
     * @param refreshCookie refresh cookie name=value
     */
    protected record AuthSession(String accessToken, String refreshCookie) {
    }

    /**
     * Login request payload for test helper.
     *
     * @param username username
     * @param password password
     */
    private record LoginRequestBody(String username, String password) {
    }
}
