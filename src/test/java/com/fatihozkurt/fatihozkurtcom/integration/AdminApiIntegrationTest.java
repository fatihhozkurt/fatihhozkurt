package com.fatihozkurt.fatihozkurtcom.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminAuditEvent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessage;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEvent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEventType;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecuritySeverity;
import com.fatihozkurt.fatihozkurtcom.domain.entity.VisitEvent;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AdminAuditEventRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactMessageRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.HeroContentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.SecurityEventRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.VisitEventRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for admin content, analytics and security flows.
 */
class AdminApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private HeroContentRepository heroContentRepository;

    @Autowired
    private VisitEventRepository visitEventRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private AdminAuditEventRepository adminAuditEventRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @BeforeEach
    void beforeEach() {
        cleanDatabase();
    }

    @Test
    void adminEndpointsShouldRequireAuthenticationAndAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/content/hero"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH002"));

        mockMvc.perform(get("/api/v1/admin/content/hero").with(user("viewer").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SEC002"));
    }

    @Test
    void adminContentCrudFlowShouldWorkWithJwtAndCsrf() throws Exception {
        seedAdmin("fatih.admin", "fatih@example.com", "Admin123!");
        AuthSession auth = loginAs("fatih.admin", "Admin123!");

        mockMvc.perform(put("/api/v1/admin/content/hero")
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("welcomeText", "New Hero"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SEC002"));

        mockMvc.perform(put("/api/v1/admin/content/hero")
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("welcomeText", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL001"));

        mockMvc.perform(put("/api/v1/admin/content/hero")
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "welcomeText", "Welcome from admin",
                                "fullName", "Fatih Ozkurt"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.welcomeText").value("Welcome from admin"));

        assertThat(heroContentRepository.findAll()).hasSize(1);

        MvcResult createTech = mockMvc.perform(post("/api/v1/admin/tech-stack")
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Java",
                                "iconName", "java",
                                "category", "language",
                                "sortOrder", 1,
                                "active", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java"))
                .andReturn();

        UUID techId = UUID.fromString(objectMapper.readTree(createTech.getResponse().getContentAsString()).path("id").asText());

        mockMvc.perform(put("/api/v1/admin/tech-stack/{id}", techId)
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Java 21",
                                "iconName", "java",
                                "category", "language",
                                "sortOrder", 2,
                                "active", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java 21"));

        mockMvc.perform(delete("/api/v1/admin/tech-stack/{id}", techId)
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("deleted"));

        MvcResult createProject = mockMvc.perform(post("/api/v1/admin/projects")
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "fatihozkurt.com",
                                "category", "Platform",
                                "summary", "Personal portfolio platform.",
                                "repositoryUrl", "https://github.com/fatihozkurt/fatihozkurtcom",
                                "demoUrl", "https://fatihozkurt.com",
                                "readmeMarkdown", "# Readme",
                                "coverImageUrl", "https://cdn.example.com/cover.png",
                                "stackCsv", "Java,Spring Boot,React",
                                "sortOrder", 1,
                                "active", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("fatihozkurt.com"))
                .andReturn();

        UUID projectId = UUID.fromString(objectMapper.readTree(createProject.getResponse().getContentAsString()).path("id").asText());

        mockMvc.perform(put("/api/v1/admin/projects/{id}", projectId)
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "fatihozkurt.com v2",
                                "category", "Platform",
                                "summary", "Updated summary.",
                                "repositoryUrl", "https://github.com/fatihozkurt/fatihozkurtcom",
                                "demoUrl", "https://fatihozkurt.com",
                                "readmeMarkdown", "# Updated",
                                "coverImageUrl", "https://cdn.example.com/cover.png",
                                "stackCsv", "Java,Spring Boot,React",
                                "sortOrder", 2,
                                "active", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("fatihozkurt.com v2"));

        mockMvc.perform(delete("/api/v1/admin/projects/{id}", projectId)
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("deleted"));

        MvcResult createArticle = mockMvc.perform(post("/api/v1/admin/articles")
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Spring Security",
                                "excerpt", "Practical security patterns.",
                                "href", "https://medium.com/@fatihozkurt/article",
                                "readingTime", "8 min",
                                "sortOrder", 1,
                                "active", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Security"))
                .andReturn();

        UUID articleId = UUID.fromString(objectMapper.readTree(createArticle.getResponse().getContentAsString()).path("id").asText());

        mockMvc.perform(put("/api/v1/admin/articles/{id}", articleId)
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Spring Security Deep Dive",
                                "excerpt", "Deep dive.",
                                "href", "https://medium.com/@fatihozkurt/article",
                                "readingTime", "9 min",
                                "sortOrder", 2,
                                "active", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Security Deep Dive"));

        mockMvc.perform(delete("/api/v1/admin/articles/{id}", articleId)
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("deleted"));

        mockMvc.perform(put("/api/v1/admin/contact-profile")
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "fatih@example.com",
                                "linkedinUrl", "https://linkedin.com/in/fatihozkurt",
                                "githubUrl", "https://github.com/fatihozkurt",
                                "mediumUrl", "https://medium.com/@fatihozkurt",
                                "recipientEmail", "owner@example.com"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fatih@example.com"));

        mockMvc.perform(post("/api/v1/admin/resume/replace")
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fileName", "fatih-ozkurt-cv.pdf",
                                "objectKey", "defaults/fatih-ozkurt-cv.pdf",
                                "contentType", "application/pdf",
                                "sizeBytes", 2048
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("fatih-ozkurt-cv.pdf"));

        MockMultipartFile resumeFile = new MockMultipartFile(
                "file",
                "fatih-ozkurt-cv-v3.pdf",
                "application/pdf",
                "resume-v3".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/admin/resume/upload")
                        .file(resumeFile)
                        .with(csrf().asHeader())
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("fatih-ozkurt-cv-v3.pdf"));

        mockMvc.perform(get("/api/v1/admin/contact-messages")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminAnalyticsAndSecurityEndpointsShouldReturnSeededData() throws Exception {
        seedAdmin("fatih.admin", "fatih@example.com", "Admin123!");
        AuthSession auth = loginAs("fatih.admin", "Admin123!");

        VisitEvent visitEvent = new VisitEvent();
        visitEvent.setPath("/projects");
        visitEvent.setCountry("Turkiye");
        visitEvent.setIpAddress("10.0.0.80");
        visitEventRepository.save(visitEvent);

        SecurityEvent securityEvent = new SecurityEvent();
        securityEvent.setEventType(SecurityEventType.LOGIN_FAILED);
        securityEvent.setSeverity(SecuritySeverity.WARN);
        securityEvent.setUsername("fatih.admin");
        securityEvent.setOccurredAt(OffsetDateTime.now().minusMinutes(1));
        securityEventRepository.save(securityEvent);

        SecurityEvent resetEvent = new SecurityEvent();
        resetEvent.setEventType(SecurityEventType.RESET_REQUESTED);
        resetEvent.setSeverity(SecuritySeverity.INFO);
        resetEvent.setOccurredAt(OffsetDateTime.now().minusMinutes(1));
        securityEventRepository.save(resetEvent);

        AdminAuditEvent auditEvent = new AdminAuditEvent();
        auditEvent.setActor("fatih.admin");
        auditEvent.setAction("Updated hero");
        auditEvent.setResource("hero");
        auditEvent.setDetails("welcome");
        adminAuditEventRepository.save(auditEvent);

        ContactMessage message = new ContactMessage();
        message.setTitle("Hi");
        message.setEmail("sender@example.com");
        message.setContent("Message");
        contactMessageRepository.save(message);

        mockMvc.perform(get("/api/v1/admin/analytics/overview")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitsToday").isNumber())
                .andExpect(jsonPath("$.failedLoginsToday").isNumber());

        mockMvc.perform(get("/api/v1/admin/analytics/country-distribution")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("Turkiye"));

        mockMvc.perform(get("/api/v1/admin/analytics/visits-trend")
                        .queryParam("days", "7")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/v1/admin/analytics/top-pages")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value("/projects"));

        mockMvc.perform(get("/api/v1/admin/analytics/security-events")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").isNotEmpty());

        mockMvc.perform(get("/api/v1/admin/analytics/audit-events")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actor").value("fatih.admin"));

        mockMvc.perform(get("/api/v1/admin/analytics/mail-deliveries")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/v1/admin/security/events")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/v1/admin/security/failed-logins")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("LOGIN_FAILED"));

        mockMvc.perform(get("/api/v1/admin/security/reset-events")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value(org.hamcrest.Matchers.startsWith("RESET_")));

        mockMvc.perform(get("/api/v1/admin/security/audit-events")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resource").value("hero"));

        mockMvc.perform(get("/api/v1/admin/dashboard/overview")
                        .header("Authorization", bearer(auth.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.securityEventsToday").isNumber());
    }
}
