package com.fatihozkurt.fatihozkurtcom.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessageStatus;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MediumArticle;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ProjectItem;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactMessageRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MediumArticleRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ProjectItemRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.VisitEventRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * Integration tests for public API flows.
 */
class PublicApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProjectItemRepository projectItemRepository;

    @Autowired
    private MediumArticleRepository mediumArticleRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private VisitEventRepository visitEventRepository;

    @BeforeEach
    void beforeEach() {
        cleanDatabase();
    }

    @Test
    void publicEndpointsShouldReturnContentAndAcceptPublicEvents() throws Exception {
        ProjectItem projectItem = new ProjectItem();
        projectItem.setTitle("Observer Platform");
        projectItem.setCategory("Backend");
        projectItem.setSummary("Security and observability focused backend.");
        projectItem.setRepositoryUrl("https://github.com/fatihozkurt/observer");
        projectItem.setCoverImageUrl("defaults/project-surface.svg");
        projectItem.setStackCsv("Java,Spring Boot,PostgreSQL");
        projectItem.setSortOrder(1);
        projectItem.setActive(true);
        projectItem = projectItemRepository.save(projectItem);

        MediumArticle article = new MediumArticle();
        article.setTitle("Spring Security Notes");
        article.setExcerpt("Practical hardening patterns.");
        article.setHref("https://medium.com/@fatihozkurt/sample");
        article.setReadingTime("6 min");
        article.setPublishedAt(LocalDate.now());
        article.setSortOrder(1);
        article.setActive(true);
        mediumArticleRepository.save(article);

        mockMvc.perform(get("/api/v1/public/hero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Fatih Ozkurt"));

        mockMvc.perform(get("/api/v1/public/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").isNotEmpty());

        mockMvc.perform(get("/api/v1/public/tech-stack"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Observer Platform"))
                .andExpect(jsonPath("$[0].coverImageUrl").value(org.hamcrest.Matchers.startsWith("/api/v1/public/assets/projects/")));

        mockMvc.perform(get("/api/v1/public/projects/{id}", projectItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Observer Platform"))
                .andExpect(jsonPath("$.stack[0]").value("Java"));

        mockMvc.perform(get("/api/v1/public/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Security Notes"));

        mockMvc.perform(get("/api/v1/public/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value("/api/v1/public/resume/download"));

        mockMvc.perform(get("/api/v1/public/resume/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));

        mockMvc.perform(get("/api/v1/public/contact-profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").isNotEmpty());

        mockMvc.perform(get("/api/v1/public/assets/projects/defaults/project-surface.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));

        mockMvc.perform(post("/api/v1/public/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", nextIp())
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Selam",
                                "email", "sender@example.com",
                                "content", "Merhaba"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("accepted"));

        assertThat(contactMessageRepository.findAll()).hasSize(1);
        assertThat(contactMessageRepository.findAll().getFirst().getStatus()).isEqualTo(ContactMessageStatus.DELIVERED);

        mockMvc.perform(post("/api/v1/public/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", nextIp())
                        .content(objectMapper.writeValueAsString(Map.of("path", "/projects", "country", "Turkiye"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("recorded"));

        assertThat(visitEventRepository.count()).isEqualTo(1);
    }

    @Test
    void getProjectDetailShouldReturnUsr001WhenProjectDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/public/projects/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USR001"));
    }

    @Test
    void contactMessageValidationShouldReturnVal001() throws Exception {
        mockMvc.perform(post("/api/v1/public/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "",
                                "email", "not-an-email",
                                "content", ""
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL001"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
