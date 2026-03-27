package com.fatihozkurt.fatihozkurtcom.api.controller;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.AboutUpdateRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ArticleUpsertRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ContactProfileUpdateRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.HeroUpdateRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ProjectUpsertRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ResumeReplaceRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.TechStackUpsertRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.AboutResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ContactProfileResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.HeroResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.MediumArticleResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ProjectSummaryResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ResumeResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.TechStackItemResponse;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessage;
import com.fatihozkurt.fatihozkurtcom.service.content.ContentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * Exposes admin content management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminContentController {

    private final ContentService contentService;

    /**
     * Returns current hero content.
     *
     * @return hero response
     */
    @GetMapping("/content/hero")
    public HeroResponse getHero() {
        return contentService.getHero();
    }

    /**
     * Partially updates hero content.
     *
     * @param request update request
     * @param authentication authentication
     * @return updated response
     */
    @PutMapping("/content/hero")
    public HeroResponse updateHero(@Valid @RequestBody HeroUpdateRequest request, Authentication authentication) {
        return contentService.updateHero(request, actor(authentication));
    }

    /**
     * Returns about content.
     *
     * @return about response
     */
    @GetMapping("/content/about")
    public AboutResponse getAbout() {
        return contentService.getAbout();
    }

    /**
     * Partially updates about content.
     *
     * @param request update request
     * @param authentication authentication
     * @return updated response
     */
    @PutMapping("/content/about")
    public AboutResponse updateAbout(@Valid @RequestBody AboutUpdateRequest request, Authentication authentication) {
        return contentService.updateAbout(request, actor(authentication));
    }

    /**
     * Returns contact profile.
     *
     * @return profile response
     */
    @GetMapping("/contact-profile")
    public ContactProfileResponse getContactProfile() {
        return contentService.getContactProfile();
    }

    /**
     * Partially updates contact profile.
     *
     * @param request update request
     * @param authentication authentication
     * @return updated profile
     */
    @PutMapping("/contact-profile")
    public ContactProfileResponse updateContactProfile(@Valid @RequestBody ContactProfileUpdateRequest request, Authentication authentication) {
        return contentService.updateContactProfile(request, actor(authentication));
    }

    /**
     * Returns tech stack items for admin.
     *
     * @return tech stack list
     */
    @GetMapping("/tech-stack")
    public List<TechStackItemResponse> getTechStack() {
        return contentService.getAllTechStackForAdmin();
    }

    /**
     * Creates tech stack item.
     *
     * @param request upsert request
     * @param authentication authentication
     * @return created response
     */
    @PostMapping("/tech-stack")
    public TechStackItemResponse createTechItem(@Valid @RequestBody TechStackUpsertRequest request, Authentication authentication) {
        return contentService.createTechItem(request, actor(authentication));
    }

    /**
     * Updates tech stack item.
     *
     * @param id item id
     * @param request upsert request
     * @param authentication authentication
     * @return updated response
     */
    @PutMapping("/tech-stack/{id}")
    public TechStackItemResponse updateTechItem(@PathVariable UUID id, @Valid @RequestBody TechStackUpsertRequest request, Authentication authentication) {
        return contentService.updateTechItem(id, request, actor(authentication));
    }

    /**
     * Deletes tech stack item.
     *
     * @param id item id
     * @param authentication authentication
     * @return status payload
     */
    @DeleteMapping("/tech-stack/{id}")
    public Map<String, String> deleteTechItem(@PathVariable UUID id, Authentication authentication) {
        contentService.deleteTechItem(id, actor(authentication));
        return Map.of("status", "deleted");
    }

    /**
     * Returns all projects for admin.
     *
     * @return project list
     */
    @GetMapping("/projects")
    public List<ProjectSummaryResponse> getProjects() {
        return contentService.getAllProjectsForAdmin();
    }

    /**
     * Creates project.
     *
     * @param request upsert request
     * @param authentication authentication
     * @return created project
     */
    @PostMapping("/projects")
    public ProjectSummaryResponse createProject(@Valid @RequestBody ProjectUpsertRequest request, Authentication authentication) {
        return contentService.createProject(request, actor(authentication));
    }

    /**
     * Updates project.
     *
     * @param id project id
     * @param request upsert request
     * @param authentication authentication
     * @return updated project
     */
    @PutMapping("/projects/{id}")
    public ProjectSummaryResponse updateProject(@PathVariable UUID id, @Valid @RequestBody ProjectUpsertRequest request, Authentication authentication) {
        return contentService.updateProject(id, request, actor(authentication));
    }

    /**
     * Deletes project.
     *
     * @param id project id
     * @param authentication authentication
     * @return status payload
     */
    @DeleteMapping("/projects/{id}")
    public Map<String, String> deleteProject(@PathVariable UUID id, Authentication authentication) {
        contentService.deleteProject(id, actor(authentication));
        return Map.of("status", "deleted");
    }

    /**
     * Returns all articles for admin.
     *
     * @return article list
     */
    @GetMapping("/articles")
    public List<MediumArticleResponse> getArticles() {
        return contentService.getAllArticlesForAdmin();
    }

    /**
     * Creates article.
     *
     * @param request upsert request
     * @param authentication authentication
     * @return created article
     */
    @PostMapping("/articles")
    public MediumArticleResponse createArticle(@Valid @RequestBody ArticleUpsertRequest request, Authentication authentication) {
        return contentService.createArticle(request, actor(authentication));
    }

    /**
     * Updates article.
     *
     * @param id article id
     * @param request upsert request
     * @param authentication authentication
     * @return updated article
     */
    @PutMapping("/articles/{id}")
    public MediumArticleResponse updateArticle(@PathVariable UUID id, @Valid @RequestBody ArticleUpsertRequest request, Authentication authentication) {
        return contentService.updateArticle(id, request, actor(authentication));
    }

    /**
     * Deletes article.
     *
     * @param id article id
     * @param authentication authentication
     * @return status payload
     */
    @DeleteMapping("/articles/{id}")
    public Map<String, String> deleteArticle(@PathVariable UUID id, Authentication authentication) {
        contentService.deleteArticle(id, actor(authentication));
        return Map.of("status", "deleted");
    }

    /**
     * Returns current resume metadata.
     *
     * @return resume response
     */
    @GetMapping("/resume")
    public ResumeResponse getResume() {
        return contentService.getResume();
    }

    /**
     * Replaces active resume metadata.
     *
     * @param request replace request
     * @param authentication authentication
     * @return updated resume response
     */
    @PostMapping("/resume/replace")
    public ResumeResponse replaceResume(@Valid @RequestBody ResumeReplaceRequest request, Authentication authentication) {
        return contentService.replaceResume(request, actor(authentication));
    }

    /**
     * Uploads and activates a new resume file.
     *
     * @param file resume file
     * @param authentication authentication
     * @return updated resume response
     */
    @PostMapping(value = "/resume/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResponse uploadResume(@RequestPart("file") MultipartFile file, Authentication authentication) {
        return contentService.uploadResume(file, actor(authentication));
    }

    /**
     * Returns contact messages.
     *
     * @return message list
     */
    @GetMapping("/contact-messages")
    public List<ContactMessage> getContactMessages() {
        return contentService.getContactMessages();
    }

    private String actor(Authentication authentication) {
        return authentication != null ? String.valueOf(authentication.getPrincipal()) : "system";
    }
}
