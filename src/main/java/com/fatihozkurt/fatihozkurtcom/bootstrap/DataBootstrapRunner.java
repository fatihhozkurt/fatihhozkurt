package com.fatihozkurt.fatihozkurtcom.bootstrap;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AboutContent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminRole;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactProfile;
import com.fatihozkurt.fatihozkurtcom.domain.entity.CvDocument;
import com.fatihozkurt.fatihozkurtcom.domain.entity.HeroContent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MediumArticle;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ProjectItem;
import com.fatihozkurt.fatihozkurtcom.domain.entity.TechStackItem;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AboutContentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AdminUserRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactProfileRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.CvDocumentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.HeroContentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MediumArticleRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ProjectItemRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.TechStackItemRepository;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.storage.ObjectStorageService;
import com.fatihozkurt.fatihozkurtcom.storage.StorageBucket;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds baseline data for local and first startup scenarios.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataBootstrapRunner implements CommandLineRunner {

    private static final String DEFAULT_RESUME_OBJECT_KEY = "defaults/fatih-ozkurt-cv.pdf";
    private static final String DEFAULT_PROJECT_COVER_OBJECT_KEY = "defaults/project-surface.svg";

    private final AdminUserRepository adminUserRepository;
    private final HeroContentRepository heroContentRepository;
    private final AboutContentRepository aboutContentRepository;
    private final TechStackItemRepository techStackItemRepository;
    private final ProjectItemRepository projectItemRepository;
    private final MediumArticleRepository mediumArticleRepository;
    private final CvDocumentRepository cvDocumentRepository;
    private final ContactProfileRepository contactProfileRepository;
    private final ObjectStorageService objectStorageService;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    /**
     * Seeds bootstrap admin and starter content.
     *
     * @param args args
     */
    @Override
    public void run(String... args) {
        seedStorageAssets();
        seedAdmin();
        seedContent();
    }

    private void seedStorageAssets() {
        objectStorageService.ensureBucketExists(StorageBucket.PUBLIC);
        objectStorageService.ensureBucketExists(StorageBucket.PROJECT);
        objectStorageService.ensureBucketExists(StorageBucket.RESUME);
        uploadIfMissing(StorageBucket.PROJECT, DEFAULT_PROJECT_COVER_OBJECT_KEY, "storage/default-project-surface.svg", "image/svg+xml");
        uploadIfMissing(StorageBucket.RESUME, DEFAULT_RESUME_OBJECT_KEY, "storage/default-cv.pdf", "application/pdf");
    }

    private void seedAdmin() {
        if (adminUserRepository.count() > 0) {
            return;
        }

        if (!appProperties.getAdmin().getBootstrap().isEnabled()) {
            if (isProductProfile()) {
                throw new IllegalStateException("No admin user found. Enable bootstrap once or create an admin user before starting product profile.");
            }
            log.warn("No admin user found and bootstrap admin seeding is disabled.");
            return;
        }

        String username = normalize(appProperties.getAdmin().getBootstrap().getUsername());
        String rawPassword = appProperties.getAdmin().getBootstrap().getPassword();
        String email = normalize(appProperties.getAdmin().getBootstrap().getEmail());

        if (username == null || rawPassword == null || rawPassword.isBlank() || email == null) {
            throw new IllegalStateException("Admin bootstrap is enabled but username/password/email are not fully configured.");
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setEmail(email);
        adminUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        adminUser.setRole(AdminRole.ROLE_ADMIN);
        adminUser.setActive(true);
        adminUserRepository.save(adminUser);
        log.info("Bootstrap admin created username={}", adminUser.getUsername());
    }

    private void seedContent() {
        if (heroContentRepository.count() == 0) {
            HeroContent heroContent = new HeroContent();
            heroContent.setWelcomeText("Welcome to my corner of the internet");
            heroContent.setFullName("Fatih Ozkurt");
            heroContent.setTitle("Java Backend Developer");
            heroContent.setDescription("Secure APIs, disciplined service design, and reliable backend delivery.");
            heroContent.setCtaLabel("Explore");
            heroContentRepository.save(heroContent);
        }
        if (aboutContentRepository.count() == 0) {
            AboutContent aboutContent = new AboutContent();
            aboutContent.setEyebrow("About");
            aboutContent.setTitle("Backend systems with production discipline.");
            aboutContent.setDescription("I build secure, observable, and maintainable backend services.");
            aboutContentRepository.save(aboutContent);
        }
        if (techStackItemRepository.count() == 0) {
            List<String> tech = List.of("Java", "Spring Boot", "Spring Security", "PostgreSQL", "Redis", "OpenSearch", "Docker", "MinIO");
            for (int i = 0; i < tech.size(); i++) {
                TechStackItem item = new TechStackItem();
                item.setName(tech.get(i));
                item.setIconName(tech.get(i));
                item.setCategory("backend");
                item.setSortOrder(i + 1);
                item.setActive(true);
                techStackItemRepository.save(item);
            }
        }
        if (projectItemRepository.count() == 0) {
            ProjectItem p1 = new ProjectItem();
            p1.setTitle("Personal Portfolio Platform");
            p1.setCategory("Backend Core");
            p1.setSummary("Secure personal platform with admin-managed content and analytics-ready backend.");
            p1.setRepositoryUrl("https://github.com/fatihozkurt");
            p1.setReadmeMarkdown("## Personal Portfolio Platform\nBackend-first architecture with secure admin workflows.");
            p1.setCoverImageUrl(DEFAULT_PROJECT_COVER_OBJECT_KEY);
            p1.setStackCsv("Java,Spring Boot,PostgreSQL,Redis,React,Tailwind");
            p1.setSortOrder(1);
            p1.setActive(true);
            projectItemRepository.save(p1);
        }
        projectItemRepository.findAll().forEach(project -> {
            if ("/project-surface.svg".equals(project.getCoverImageUrl())) {
                project.setCoverImageUrl(DEFAULT_PROJECT_COVER_OBJECT_KEY);
                projectItemRepository.save(project);
            }
        });
        if (mediumArticleRepository.count() == 0) {
            MediumArticle article = new MediumArticle();
            article.setTitle("How secure API contracts stay readable");
            article.setExcerpt("Practical patterns for balancing security and maintainability in Spring APIs.");
            article.setHref("https://medium.com/@fatihozkurt");
            article.setReadingTime("6 min read");
            article.setPublishedAt(LocalDate.now().minusDays(12));
            article.setSortOrder(1);
            article.setActive(true);
            mediumArticleRepository.save(article);
        }
        if (cvDocumentRepository.count() == 0) {
            CvDocument cvDocument = new CvDocument();
            cvDocument.setFileName("fatih-ozkurt-cv.pdf");
            cvDocument.setObjectKey(DEFAULT_RESUME_OBJECT_KEY);
            cvDocument.setContentType("application/pdf");
            cvDocument.setSizeBytes(0L);
            cvDocument.setActive(true);
            cvDocumentRepository.save(cvDocument);
        }
        cvDocumentRepository.findFirstByActiveTrueOrderByUpdatedAtDesc().ifPresent(cvDocument -> {
            if ("cv/fatih-ozkurt-cv.pdf".equals(cvDocument.getObjectKey())) {
                cvDocument.setObjectKey(DEFAULT_RESUME_OBJECT_KEY);
                cvDocumentRepository.save(cvDocument);
            }
        });
        if (contactProfileRepository.count() == 0) {
            ContactProfile profile = new ContactProfile();
            profile.setEmail("fatih@example.com");
            profile.setLinkedinUrl("https://linkedin.com/in/fatihozkurt");
            profile.setGithubUrl("https://github.com/fatihozkurt");
            profile.setMediumUrl("https://medium.com/@fatihozkurt");
            profile.setRecipientEmail("fatih@example.com");
            contactProfileRepository.save(profile);
        }
    }

    private void uploadIfMissing(StorageBucket bucket, String objectKey, String classpathResource, String contentType) {
        if (objectStorageService.exists(bucket, objectKey)) {
            return;
        }
        ClassPathResource resource = new ClassPathResource(classpathResource);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            objectStorageService.putObject(bucket, objectKey, bytes, contentType);
            log.info("Seeded storage asset bucket={} key={}", bucket, objectKey);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to seed storage asset " + classpathResource, ex);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isProductProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("product".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
