package com.fatihozkurt.fatihozkurtcom.service.content;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.AboutUpdateRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ArticleUpsertRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ContactProfileUpdateRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.HeroUpdateRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ProjectUpsertRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ResumeReplaceRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.TechStackUpsertRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.AboutResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ContactMessageRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ContactProfileResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.HeroResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.MediumArticleResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ProjectDetailResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ProjectSummaryResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ResumeResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.TechStackItemResponse;
import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AboutContent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessage;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessageStatus;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactProfile;
import com.fatihozkurt.fatihozkurtcom.domain.entity.CvDocument;
import com.fatihozkurt.fatihozkurtcom.domain.entity.HeroContent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MediumArticle;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ProjectItem;
import com.fatihozkurt.fatihozkurtcom.domain.entity.TechStackItem;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AboutContentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactMessageRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactProfileRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.CvDocumentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.HeroContentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MediumArticleRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ProjectItemRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.TechStackItemRepository;
import com.fatihozkurt.fatihozkurtcom.storage.ObjectStorageService;
import com.fatihozkurt.fatihozkurtcom.storage.StorageBucket;
import com.fatihozkurt.fatihozkurtcom.storage.StorageObjectKeyPolicy;
import com.fatihozkurt.fatihozkurtcom.storage.StoredAsset;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.fatihozkurt.fatihozkurtcom.security.ratelimit.RateLimitService;
import com.fatihozkurt.fatihozkurtcom.service.AdminAuditService;
import com.fatihozkurt.fatihozkurtcom.service.mail.MailDeliveryService;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

/**
 * Provides public and admin-facing content management operations.
 */
@Service
@RequiredArgsConstructor
public class ContentService {

    private static final String DEFAULT_RESUME_OBJECT_KEY = "defaults/fatih-ozkurt-cv.pdf";
    private static final String DEFAULT_PROJECT_COVER_OBJECT_KEY = "defaults/project-surface.svg";

    private final HeroContentRepository heroContentRepository;
    private final AboutContentRepository aboutContentRepository;
    private final TechStackItemRepository techStackItemRepository;
    private final ProjectItemRepository projectItemRepository;
    private final MediumArticleRepository mediumArticleRepository;
    private final CvDocumentRepository cvDocumentRepository;
    private final ContactProfileRepository contactProfileRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final MailDeliveryService mailDeliveryService;
    private final AdminAuditService adminAuditService;
    private final RateLimitService rateLimitService;
    private final ObjectStorageService objectStorageService;

    /**
     * Returns current public hero content.
     *
     * @return hero response
     */
    @Transactional(readOnly = true)
    public HeroResponse getHero() {
        HeroContent heroContent = heroContentRepository.findAll().stream().findFirst().orElseGet(this::createDefaultHero);
        return new HeroResponse(
                heroContent.getWelcomeText(),
                heroContent.getFullName(),
                heroContent.getTitle(),
                heroContent.getDescription(),
                heroContent.getCtaLabel()
        );
    }

    /**
     * Returns public about content.
     *
     * @return about response
     */
    @Transactional(readOnly = true)
    public AboutResponse getAbout() {
        AboutContent aboutContent = aboutContentRepository.findAll().stream().findFirst().orElseGet(this::createDefaultAbout);
        return new AboutResponse(aboutContent.getEyebrow(), aboutContent.getTitle(), aboutContent.getDescription());
    }

    /**
     * Returns active public tech stack items.
     *
     * @return list response
     */
    @Transactional(readOnly = true)
    public List<TechStackItemResponse> getTechStack() {
        return techStackItemRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(item -> new TechStackItemResponse(item.getId(), item.getName(), item.getIconName(), item.getCategory()))
                .toList();
    }

    /**
     * Returns active project summaries.
     *
     * @return summaries
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getProjects() {
        return projectItemRepository.findByActiveTrueOrderBySortOrderAscTitleAsc().stream()
                .map(this::toProjectSummary)
                .toList();
    }

    /**
     * Returns project detail by id.
     *
     * @param id project id
     * @return detail response
     */
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(UUID id) {
        ProjectItem projectItem = projectItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USR001));
        return new ProjectDetailResponse(
                projectItem.getId(),
                projectItem.getTitle(),
                projectItem.getCategory(),
                projectItem.getSummary(),
                projectItem.getRepositoryUrl(),
                projectItem.getDemoUrl(),
                projectItem.getReadmeMarkdown(),
                resolveProjectCoverUrl(projectItem.getCoverImageUrl()),
                parseStack(projectItem.getStackCsv())
        );
    }

    /**
     * Returns active Medium article cards.
     *
     * @return articles
     */
    @Transactional(readOnly = true)
    public List<MediumArticleResponse> getArticles() {
        return mediumArticleRepository.findByActiveTrueOrderBySortOrderAscTitleAsc().stream()
                .map(article -> new MediumArticleResponse(
                        article.getId(),
                        article.getTitle(),
                        article.getExcerpt(),
                        article.getHref(),
                        article.getReadingTime(),
                        article.getPublishedAt()
                ))
                .toList();
    }

    /**
     * Returns active CV metadata.
     *
     * @return resume response
     */
    @Transactional(readOnly = true)
    public ResumeResponse getResume() {
        CvDocument cvDocument = cvDocumentRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .orElseGet(this::createDefaultCvDocument);
        return new ResumeResponse(
                cvDocument.getFileName(),
                cvDocument.getContentType(),
                cvDocument.getSizeBytes(),
                "/api/v1/public/resume/download"
        );
    }

    /**
     * Downloads active resume binary from object storage.
     *
     * @return stored asset
     */
    @Transactional(readOnly = true)
    public StoredAsset downloadResumeAsset() {
        CvDocument cvDocument = cvDocumentRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .orElseGet(this::createDefaultCvDocument);
        String objectKey = StorageObjectKeyPolicy.requireValid(cvDocument.getObjectKey());
        return objectStorageService.getObject(StorageBucket.RESUME, objectKey);
    }

    /**
     * Downloads project cover binary from object storage.
     *
     * @param objectKey storage object key
     * @return stored asset
     */
    @Transactional(readOnly = true)
    public StoredAsset downloadProjectCover(String objectKey) {
        return objectStorageService.getObject(StorageBucket.PROJECT, StorageObjectKeyPolicy.requireValid(objectKey));
    }

    /**
     * Returns public contact profile.
     *
     * @return contact profile
     */
    @Transactional(readOnly = true)
    public ContactProfileResponse getContactProfile() {
        ContactProfile contactProfile = contactProfileRepository.findAll().stream().findFirst().orElseGet(this::createDefaultContactProfile);
        return new ContactProfileResponse(
                contactProfile.getEmail(),
                contactProfile.getLinkedinUrl(),
                contactProfile.getGithubUrl(),
                contactProfile.getMediumUrl()
        );
    }

    /**
     * Handles inbound contact form and sends outbound email.
     *
     * @param request contact request
     * @param ipAddress request ip
     */
    @Transactional
    public void submitContactMessage(ContactMessageRequest request, String ipAddress) {
        rateLimitService.checkContact(ipAddress + ":" + request.email().toLowerCase());
        ContactMessage message = new ContactMessage();
        message.setTitle(request.title());
        message.setEmail(request.email());
        message.setContent(request.content());
        message.setStatus(ContactMessageStatus.RECEIVED);
        contactMessageRepository.save(message);

        ContactProfile contactProfile = contactProfileRepository.findAll().stream().findFirst().orElseGet(this::createDefaultContactProfile);
        mailDeliveryService.sendContactMessage(contactProfile.getRecipientEmail(), request.title(), request.content());
        message.setStatus(ContactMessageStatus.DELIVERED);
        contactMessageRepository.save(message);
    }

    /**
     * Returns all tech stack items for admin.
     *
     * @return list
     */
    @Transactional(readOnly = true)
    public List<TechStackItemResponse> getAllTechStackForAdmin() {
        return techStackItemRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(item -> new TechStackItemResponse(item.getId(), item.getName(), item.getIconName(), item.getCategory()))
                .toList();
    }

    /**
     * Returns all projects for admin.
     *
     * @return list
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getAllProjectsForAdmin() {
        return projectItemRepository.findAllByOrderBySortOrderAscTitleAsc().stream()
                .map(this::toProjectSummary)
                .toList();
    }

    /**
     * Returns all articles for admin.
     *
     * @return list
     */
    @Transactional(readOnly = true)
    public List<MediumArticleResponse> getAllArticlesForAdmin() {
        return mediumArticleRepository.findAllByOrderBySortOrderAscTitleAsc().stream()
                .map(article -> new MediumArticleResponse(
                        article.getId(),
                        article.getTitle(),
                        article.getExcerpt(),
                        article.getHref(),
                        article.getReadingTime(),
                        article.getPublishedAt()
                ))
                .toList();
    }

    /**
     * Partially updates hero content.
     *
     * @param request update request
     * @param actor actor username
     * @return updated response
     */
    @Transactional
    public HeroResponse updateHero(HeroUpdateRequest request, String actor) {
        HeroContent heroContent = heroContentRepository.findAll().stream().findFirst().orElseGet(this::createDefaultHero);
        if (request.welcomeText() != null) {
            heroContent.setWelcomeText(request.welcomeText());
        }
        if (request.fullName() != null) {
            heroContent.setFullName(request.fullName());
        }
        if (request.title() != null) {
            heroContent.setTitle(request.title());
        }
        if (request.description() != null) {
            heroContent.setDescription(request.description());
        }
        if (request.ctaLabel() != null) {
            heroContent.setCtaLabel(request.ctaLabel());
        }
        heroContentRepository.save(heroContent);
        adminAuditService.log(actor, "Updated hero content", "hero", "Hero fields updated");
        return getHero();
    }

    /**
     * Partially updates about content.
     *
     * @param request update request
     * @param actor actor username
     * @return updated response
     */
    @Transactional
    public AboutResponse updateAbout(AboutUpdateRequest request, String actor) {
        AboutContent aboutContent = aboutContentRepository.findAll().stream().findFirst().orElseGet(this::createDefaultAbout);
        if (request.eyebrow() != null) {
            aboutContent.setEyebrow(request.eyebrow());
        }
        if (request.title() != null) {
            aboutContent.setTitle(request.title());
        }
        if (request.description() != null) {
            aboutContent.setDescription(request.description());
        }
        aboutContentRepository.save(aboutContent);
        adminAuditService.log(actor, "Updated about content", "about", "About fields updated");
        return getAbout();
    }

    /**
     * Creates a tech stack item.
     *
     * @param request upsert request
     * @param actor actor username
     * @return created item
     */
    @Transactional
    public TechStackItemResponse createTechItem(TechStackUpsertRequest request, String actor) {
        TechStackItem item = new TechStackItem();
        item.setName(request.name());
        item.setIconName(request.iconName());
        item.setCategory(request.category());
        item.setSortOrder(request.sortOrder());
        item.setActive(request.active());
        techStackItemRepository.save(item);
        adminAuditService.log(actor, "Created tech item", "tech-stack", item.getName());
        return new TechStackItemResponse(item.getId(), item.getName(), item.getIconName(), item.getCategory());
    }

    /**
     * Updates a tech stack item.
     *
     * @param id item id
     * @param request upsert request
     * @param actor actor username
     * @return updated item
     */
    @Transactional
    public TechStackItemResponse updateTechItem(UUID id, TechStackUpsertRequest request, String actor) {
        TechStackItem item = techStackItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USR001));
        item.setName(request.name());
        item.setIconName(request.iconName());
        item.setCategory(request.category());
        item.setSortOrder(request.sortOrder());
        item.setActive(request.active());
        techStackItemRepository.save(item);
        adminAuditService.log(actor, "Updated tech item", "tech-stack", item.getName());
        return new TechStackItemResponse(item.getId(), item.getName(), item.getIconName(), item.getCategory());
    }

    /**
     * Deletes a tech stack item.
     *
     * @param id item id
     * @param actor actor username
     */
    @Transactional
    public void deleteTechItem(UUID id, String actor) {
        TechStackItem item = techStackItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USR001));
        techStackItemRepository.delete(item);
        adminAuditService.log(actor, "Deleted tech item", "tech-stack", item.getName());
    }

    /**
     * Creates a project item.
     *
     * @param request upsert request
     * @param actor actor username
     * @return project summary
     */
    @Transactional
    public ProjectSummaryResponse createProject(ProjectUpsertRequest request, String actor) {
        ProjectItem projectItem = new ProjectItem();
        applyProject(projectItem, request);
        projectItemRepository.save(projectItem);
        adminAuditService.log(actor, "Created project", "project", projectItem.getTitle());
        return toProjectSummary(projectItem);
    }

    /**
     * Updates a project item.
     *
     * @param id project id
     * @param request upsert request
     * @param actor actor username
     * @return project summary
     */
    @Transactional
    public ProjectSummaryResponse updateProject(UUID id, ProjectUpsertRequest request, String actor) {
        ProjectItem projectItem = projectItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USR001));
        applyProject(projectItem, request);
        projectItemRepository.save(projectItem);
        adminAuditService.log(actor, "Updated project", "project", projectItem.getTitle());
        return toProjectSummary(projectItem);
    }

    /**
     * Deletes a project item.
     *
     * @param id project id
     * @param actor actor username
     */
    @Transactional
    public void deleteProject(UUID id, String actor) {
        ProjectItem projectItem = projectItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USR001));
        projectItemRepository.delete(projectItem);
        adminAuditService.log(actor, "Deleted project", "project", projectItem.getTitle());
    }

    /**
     * Creates an article item.
     *
     * @param request upsert request
     * @param actor actor username
     * @return article response
     */
    @Transactional
    public MediumArticleResponse createArticle(ArticleUpsertRequest request, String actor) {
        MediumArticle article = new MediumArticle();
        applyArticle(article, request);
        mediumArticleRepository.save(article);
        adminAuditService.log(actor, "Created article", "article", article.getTitle());
        return toArticleResponse(article);
    }

    /**
     * Updates an article item.
     *
     * @param id article id
     * @param request upsert request
     * @param actor actor username
     * @return article response
     */
    @Transactional
    public MediumArticleResponse updateArticle(UUID id, ArticleUpsertRequest request, String actor) {
        MediumArticle article = mediumArticleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USR001));
        applyArticle(article, request);
        mediumArticleRepository.save(article);
        adminAuditService.log(actor, "Updated article", "article", article.getTitle());
        return toArticleResponse(article);
    }

    /**
     * Deletes an article item.
     *
     * @param id article id
     * @param actor actor username
     */
    @Transactional
    public void deleteArticle(UUID id, String actor) {
        MediumArticle article = mediumArticleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USR001));
        mediumArticleRepository.delete(article);
        adminAuditService.log(actor, "Deleted article", "article", article.getTitle());
    }

    /**
     * Replaces active CV metadata.
     *
     * @param request replace request
     * @param actor actor username
     * @return resume response
     */
    @Transactional
    public ResumeResponse replaceResume(ResumeReplaceRequest request, String actor) {
        String objectKey = StorageObjectKeyPolicy.requireValid(request.objectKey());
        if (!objectStorageService.exists(StorageBucket.RESUME, objectKey)) {
            throw new AppException(ErrorCode.USR001);
        }
        cvDocumentRepository.findFirstByActiveTrueOrderByUpdatedAtDesc().ifPresent(existing -> {
            existing.setActive(false);
            existing.setReplacedAt(OffsetDateTime.now());
            cvDocumentRepository.save(existing);
        });
        CvDocument cvDocument = new CvDocument();
        cvDocument.setFileName(request.fileName());
        cvDocument.setObjectKey(objectKey);
        cvDocument.setContentType(request.contentType());
        cvDocument.setSizeBytes(request.sizeBytes());
        cvDocument.setActive(true);
        cvDocumentRepository.save(cvDocument);
        adminAuditService.log(actor, "Replaced resume asset", "resume", request.fileName());
        return getResume();
    }

    /**
     * Uploads and activates a new resume file.
     *
     * @param file resume file
     * @param actor actor username
     * @return resume response
     */
    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, String actor) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VAL001);
        }
        String originalName = file.getOriginalFilename();
        String fileName = StringUtils.hasText(originalName) ? originalName.trim() : "resume.pdf";
        String objectKey = "admin-upload/" + OffsetDateTime.now().toEpochSecond() + "-" + sanitizeFileName(fileName);
        try {
            objectStorageService.putObject(
                    StorageBucket.RESUME,
                    objectKey,
                    file.getBytes(),
                    StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream"
            );
        } catch (IOException ex) {
            throw new AppException(ErrorCode.SYS002);
        }
        ResumeReplaceRequest request = new ResumeReplaceRequest(
                fileName,
                objectKey,
                StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream",
                file.getSize()
        );
        return replaceResume(request, actor);
    }

    /**
     * Partially updates contact profile.
     *
     * @param request update request
     * @param actor actor username
     * @return updated profile
     */
    @Transactional
    public ContactProfileResponse updateContactProfile(ContactProfileUpdateRequest request, String actor) {
        ContactProfile profile = contactProfileRepository.findAll().stream().findFirst().orElseGet(this::createDefaultContactProfile);
        if (request.email() != null) {
            profile.setEmail(request.email());
        }
        if (request.linkedinUrl() != null) {
            profile.setLinkedinUrl(request.linkedinUrl());
        }
        if (request.githubUrl() != null) {
            profile.setGithubUrl(request.githubUrl());
        }
        if (request.mediumUrl() != null) {
            profile.setMediumUrl(request.mediumUrl());
        }
        if (request.recipientEmail() != null) {
            profile.setRecipientEmail(request.recipientEmail());
        }
        contactProfileRepository.save(profile);
        adminAuditService.log(actor, "Updated contact profile", "contact-profile", "Profile fields updated");
        return getContactProfile();
    }

    /**
     * Returns latest contact messages.
     *
     * @return messages
     */
    @Transactional(readOnly = true)
    public List<ContactMessage> getContactMessages() {
        return contactMessageRepository.findAll();
    }

    private HeroContent createDefaultHero() {
        HeroContent heroContent = new HeroContent();
        heroContent.setWelcomeText("Welcome to my corner of the internet");
        heroContent.setFullName("Fatih Ozkurt");
        heroContent.setTitle("Java Backend Developer");
        heroContent.setDescription("Secure APIs, disciplined service design, and reliable delivery.");
        heroContent.setCtaLabel("Explore");
        return heroContentRepository.save(heroContent);
    }

    private AboutContent createDefaultAbout() {
        AboutContent aboutContent = new AboutContent();
        aboutContent.setEyebrow("About");
        aboutContent.setTitle("Backend systems with production discipline.");
        aboutContent.setDescription("I build secure, observable, and maintainable backend services.");
        return aboutContentRepository.save(aboutContent);
    }

    private CvDocument createDefaultCvDocument() {
        CvDocument cvDocument = new CvDocument();
        cvDocument.setFileName("fatih-ozkurt-cv.pdf");
        cvDocument.setObjectKey(DEFAULT_RESUME_OBJECT_KEY);
        cvDocument.setContentType("application/pdf");
        cvDocument.setSizeBytes(0L);
        cvDocument.setActive(true);
        return cvDocumentRepository.save(cvDocument);
    }

    private ContactProfile createDefaultContactProfile() {
        ContactProfile profile = new ContactProfile();
        profile.setEmail("fatih@example.com");
        profile.setLinkedinUrl("https://linkedin.com/in/fatihozkurt");
        profile.setGithubUrl("https://github.com/fatihozkurt");
        profile.setMediumUrl("https://medium.com/@fatihozkurt");
        profile.setRecipientEmail("fatih@example.com");
        return contactProfileRepository.save(profile);
    }

    private void applyProject(ProjectItem projectItem, ProjectUpsertRequest request) {
        projectItem.setTitle(request.title());
        projectItem.setCategory(request.category());
        projectItem.setSummary(request.summary());
        projectItem.setRepositoryUrl(request.repositoryUrl());
        projectItem.setDemoUrl(request.demoUrl());
        projectItem.setReadmeMarkdown(request.readmeMarkdown());
        projectItem.setCoverImageUrl(normalizeCoverReference(request.coverImageUrl()));
        projectItem.setStackCsv(request.stackCsv());
        projectItem.setSortOrder(request.sortOrder());
        projectItem.setActive(request.active());
    }

    private void applyArticle(MediumArticle article, ArticleUpsertRequest request) {
        article.setTitle(request.title());
        article.setExcerpt(request.excerpt());
        article.setHref(request.href());
        article.setReadingTime(request.readingTime());
        article.setPublishedAt(request.publishedAt());
        article.setSortOrder(request.sortOrder());
        article.setActive(request.active());
    }

    private ProjectSummaryResponse toProjectSummary(ProjectItem projectItem) {
        return new ProjectSummaryResponse(
                projectItem.getId(),
                projectItem.getTitle(),
                projectItem.getCategory(),
                projectItem.getSummary(),
                projectItem.getRepositoryUrl(),
                resolveProjectCoverUrl(projectItem.getCoverImageUrl()),
                parseStack(projectItem.getStackCsv())
        );
    }

    private MediumArticleResponse toArticleResponse(MediumArticle article) {
        return new MediumArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getExcerpt(),
                article.getHref(),
                article.getReadingTime(),
                article.getPublishedAt()
        );
    }

    private String normalizeCoverReference(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (isAbsoluteUrl(trimmed) || trimmed.startsWith("/")) {
            return trimmed;
        }
        return StorageObjectKeyPolicy.requireValid(trimmed);
    }

    private String resolveProjectCoverUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return "/api/v1/public/assets/projects/" + encodeObjectKey(DEFAULT_PROJECT_COVER_OBJECT_KEY);
        }
        String trimmed = value.trim();
        if (isAbsoluteUrl(trimmed) || trimmed.startsWith("/")) {
            return trimmed;
        }
        String objectKey = StorageObjectKeyPolicy.requireValid(trimmed);
        return "/api/v1/public/assets/projects/" + encodeObjectKey(objectKey);
    }

    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String encodeObjectKey(String objectKey) {
        return Arrays.stream(objectKey.split("/"))
                .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
                .reduce((left, right) -> left + "/" + right)
                .orElse(objectKey);
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private List<String> parseStack(String stackCsv) {
        if (!StringUtils.hasText(stackCsv)) {
            return List.of();
        }
        return Arrays.stream(stackCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
