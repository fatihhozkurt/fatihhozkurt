package com.fatihozkurt.fatihozkurtcom.api.controller;

import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.AboutResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ContactMessageRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ContactProfileResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.HeroResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.MediumArticleResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ProjectDetailResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ProjectSummaryResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ResumeResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.TechStackItemResponse;
import com.fatihozkurt.fatihozkurtcom.common.web.RequestMetadataService;
import com.fatihozkurt.fatihozkurtcom.storage.StorageObjectKeyPolicy;
import com.fatihozkurt.fatihozkurtcom.storage.StoredAsset;
import com.fatihozkurt.fatihozkurtcom.service.analytics.AnalyticsService;
import com.fatihozkurt.fatihozkurtcom.service.content.ContentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes public portfolio endpoints.
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final ContentService contentService;
    private final AnalyticsService analyticsService;
    private final RequestMetadataService requestMetadataService;

    /**
     * Returns hero content.
     *
     * @return hero response
     */
    @GetMapping("/hero")
    public HeroResponse getHero() {
        return contentService.getHero();
    }

    /**
     * Returns about content.
     *
     * @return about response
     */
    @GetMapping("/about")
    public AboutResponse getAbout() {
        return contentService.getAbout();
    }

    /**
     * Returns active tech stack list.
     *
     * @return tech items
     */
    @GetMapping("/tech-stack")
    public List<TechStackItemResponse> getTechStack() {
        return contentService.getTechStack();
    }

    /**
     * Returns project summaries.
     *
     * @return projects
     */
    @GetMapping("/projects")
    public List<ProjectSummaryResponse> getProjects() {
        return contentService.getProjects();
    }

    /**
     * Returns project detail.
     *
     * @param id project id
     * @return detail
     */
    @GetMapping("/projects/{id}")
    public ProjectDetailResponse getProject(@PathVariable UUID id) {
        return contentService.getProjectDetail(id);
    }

    /**
     * Returns article cards.
     *
     * @return articles
     */
    @GetMapping("/articles")
    public List<MediumArticleResponse> getArticles() {
        return contentService.getArticles();
    }

    /**
     * Returns resume metadata.
     *
     * @return resume metadata
     */
    @GetMapping("/resume")
    public ResumeResponse getResume() {
        return contentService.getResume();
    }

    /**
     * Streams active resume file.
     *
     * @return file response
     */
    @GetMapping("/resume/download")
    public ResponseEntity<ByteArrayResource> downloadResume() {
        ResumeResponse resume = contentService.getResume();
        StoredAsset asset = contentService.downloadResumeAsset();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.fileName() + "\"")
                .contentType(MediaType.parseMediaType(asset.contentType()))
                .contentLength(asset.sizeBytes())
                .body(new ByteArrayResource(asset.bytes()));
    }

    /**
     * Returns contact profile.
     *
     * @return contact profile
     */
    @GetMapping("/contact-profile")
    public ContactProfileResponse getContactProfile() {
        return contentService.getContactProfile();
    }

    /**
     * Streams project cover asset from object storage.
     *
     * @param request request
     * @return binary response
     */
    @GetMapping("/assets/projects/**")
    public ResponseEntity<ByteArrayResource> getProjectCover(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String prefix = "/api/v1/public/assets/projects/";
        String rawKey = requestUri.startsWith(prefix) ? requestUri.substring(prefix.length()) : "";
        String objectKey = StorageObjectKeyPolicy.requireValid(URLDecoder.decode(rawKey, StandardCharsets.UTF_8));
        StoredAsset asset = contentService.downloadProjectCover(objectKey);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType(asset.contentType()))
                .contentLength(asset.sizeBytes())
                .body(new ByteArrayResource(asset.bytes()));
    }

    /**
     * Accepts contact form message.
     *
     * @param request payload
     * @param httpServletRequest request
     * @return status payload
     */
    @PostMapping("/contact-messages")
    public Map<String, String> submitContactMessage(@Valid @RequestBody ContactMessageRequest request, HttpServletRequest httpServletRequest) {
        contentService.submitContactMessage(request, requestMetadataService.resolveIp(httpServletRequest));
        return Map.of("status", "accepted");
    }

    /**
     * Accepts visit telemetry event.
     *
     * @param payload payload
     * @param request request
     * @return status payload
     */
    @PostMapping("/visits")
    public Map<String, String> recordVisit(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        analyticsService.recordVisit(
                payload.getOrDefault("path", "/"),
                payload.getOrDefault("country", "Unknown"),
                requestMetadataService.resolveIp(request),
                requestMetadataService.resolveUserAgent(request)
        );
        return Map.of("status", "recorded");
    }
}
