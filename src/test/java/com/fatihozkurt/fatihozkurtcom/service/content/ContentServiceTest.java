package com.fatihozkurt.fatihozkurtcom.service.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.HeroUpdateRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.ResumeReplaceRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ContactMessageRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.HeroResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.publicapi.ResumeResponse;
import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessage;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessageStatus;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactProfile;
import com.fatihozkurt.fatihozkurtcom.domain.entity.CvDocument;
import com.fatihozkurt.fatihozkurtcom.domain.entity.HeroContent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.ProjectItem;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AboutContentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactMessageRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactProfileRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.CvDocumentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.HeroContentRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MediumArticleRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ProjectItemRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.TechStackItemRepository;
import com.fatihozkurt.fatihozkurtcom.security.ratelimit.RateLimitService;
import com.fatihozkurt.fatihozkurtcom.service.AdminAuditService;
import com.fatihozkurt.fatihozkurtcom.service.mail.MailDeliveryService;
import com.fatihozkurt.fatihozkurtcom.storage.ObjectStorageService;
import com.fatihozkurt.fatihozkurtcom.storage.StorageBucket;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ContentService}.
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private HeroContentRepository heroContentRepository;
    @Mock
    private AboutContentRepository aboutContentRepository;
    @Mock
    private TechStackItemRepository techStackItemRepository;
    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private MediumArticleRepository mediumArticleRepository;
    @Mock
    private CvDocumentRepository cvDocumentRepository;
    @Mock
    private ContactProfileRepository contactProfileRepository;
    @Mock
    private ContactMessageRepository contactMessageRepository;
    @Mock
    private MailDeliveryService mailDeliveryService;
    @Mock
    private AdminAuditService adminAuditService;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private ObjectStorageService objectStorageService;

    private ContentService contentService;

    @BeforeEach
    void setUp() {
        contentService = new ContentService(
                heroContentRepository,
                aboutContentRepository,
                techStackItemRepository,
                projectItemRepository,
                mediumArticleRepository,
                cvDocumentRepository,
                contactProfileRepository,
                contactMessageRepository,
                mailDeliveryService,
                adminAuditService,
                rateLimitService,
                objectStorageService
        );
    }

    @Test
    void getHeroShouldCreateDefaultsWhenNoRecordExists() {
        when(heroContentRepository.findAll()).thenReturn(List.of());
        when(heroContentRepository.save(any(HeroContent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HeroResponse response = contentService.getHero();

        assertThat(response.fullName()).isEqualTo("Fatih Ozkurt");
        assertThat(response.title()).isEqualTo("Java Backend Developer");
        assertThat(response.ctaLabel()).isEqualTo("Explore");
        verify(heroContentRepository).save(any(HeroContent.class));
    }

    @Test
    void submitContactMessageShouldPersistAndMarkDelivered() {
        ContactProfile profile = new ContactProfile();
        profile.setRecipientEmail("owner@example.com");
        when(contactProfileRepository.findAll()).thenReturn(List.of(profile));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contentService.submitContactMessage(
                new ContactMessageRequest("Hello", "sender@example.com", "Can we talk?"),
                "10.0.0.20"
        );

        verify(rateLimitService).checkContact("10.0.0.20:sender@example.com");
        verify(mailDeliveryService).sendContactMessage("owner@example.com", "Hello", "Can we talk?");

        ArgumentCaptor<ContactMessage> captor = ArgumentCaptor.forClass(ContactMessage.class);
        verify(contactMessageRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().getLast().getStatus()).isEqualTo(ContactMessageStatus.DELIVERED);
    }

    @Test
    void updateHeroShouldOnlyPatchProvidedFields() {
        HeroContent heroContent = new HeroContent();
        heroContent.setWelcomeText("Old welcome");
        heroContent.setFullName("Old name");
        heroContent.setTitle("Old title");
        heroContent.setDescription("Old description");
        heroContent.setCtaLabel("Old cta");

        when(heroContentRepository.findAll()).thenReturn(List.of(heroContent));
        when(heroContentRepository.save(any(HeroContent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HeroResponse response = contentService.updateHero(
                new HeroUpdateRequest("New welcome", null, null, "New description", null),
                "fatih.admin"
        );

        assertThat(response.welcomeText()).isEqualTo("New welcome");
        assertThat(response.description()).isEqualTo("New description");
        assertThat(response.fullName()).isEqualTo("Old name");
        verify(adminAuditService).log("fatih.admin", "Updated hero content", "hero", "Hero fields updated");
    }

    @Test
    void getProjectDetailShouldThrowUsr001WhenProjectNotFound() {
        UUID id = UUID.randomUUID();
        when(projectItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contentService.getProjectDetail(id))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USR001);
    }

    @Test
    void replaceResumeShouldDeactivateOldCvAndReturnNewOne() {
        CvDocument existing = new CvDocument();
        existing.setFileName("old.pdf");
        existing.setObjectKey("cv/old.pdf");
        existing.setContentType("application/pdf");
        existing.setSizeBytes(100);
        existing.setActive(true);

        CvDocument updated = new CvDocument();
        updated.setFileName("new.pdf");
        updated.setObjectKey("cv/new.pdf");
        updated.setContentType("application/pdf");
        updated.setSizeBytes(200);
        updated.setActive(true);

        when(cvDocumentRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()).thenReturn(
                Optional.of(existing),
                Optional.of(updated)
        );
        when(objectStorageService.exists(StorageBucket.RESUME, "cv/new.pdf")).thenReturn(true);
        when(cvDocumentRepository.save(any(CvDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeResponse response = contentService.replaceResume(
                new ResumeReplaceRequest("new.pdf", "cv/new.pdf", "application/pdf", 200),
                "fatih.admin"
        );

        assertThat(existing.isActive()).isFalse();
        assertThat(existing.getReplacedAt()).isAfter(OffsetDateTime.now().minusMinutes(1));
        assertThat(response.fileName()).isEqualTo("new.pdf");
        verify(cvDocumentRepository, times(2)).save(any(CvDocument.class));
        verify(adminAuditService).log(eq("fatih.admin"), eq("Replaced resume asset"), eq("resume"), eq("new.pdf"));
    }

    @Test
    void replaceResumeShouldThrowUsr001WhenObjectDoesNotExist() {
        when(objectStorageService.exists(StorageBucket.RESUME, "cv/missing.pdf")).thenReturn(false);

        assertThatThrownBy(() -> contentService.replaceResume(
                new ResumeReplaceRequest("missing.pdf", "cv/missing.pdf", "application/pdf", 120),
                "fatih.admin"
        ))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USR001);
    }

    @Test
    void submitContactMessageShouldCreateDefaultProfileWhenMissing() {
        when(contactProfileRepository.findAll()).thenReturn(List.of());
        when(contactProfileRepository.save(any(ContactProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contentService.submitContactMessage(
                new ContactMessageRequest("Need info", "sender@example.com", "Details"),
                "10.0.0.21"
        );

        verify(contactProfileRepository).save(any(ContactProfile.class));
        verify(mailDeliveryService).sendContactMessage("fatih@example.com", "Need info", "Details");
        verify(adminAuditService, never()).log(any(), any(), any(), any());
    }

    @Test
    void getProjectDetailShouldSplitStackCsv() {
        UUID projectId = UUID.randomUUID();
        ProjectItem projectItem = new ProjectItem();
        projectItem.setId(projectId);
        projectItem.setTitle("Project");
        projectItem.setCategory("Backend");
        projectItem.setSummary("Summary");
        projectItem.setStackCsv("Java, Spring Boot, PostgreSQL");

        when(projectItemRepository.findById(projectId)).thenReturn(Optional.of(projectItem));

        assertThat(contentService.getProjectDetail(projectId).stack()).containsExactly("Java", "Spring Boot", "PostgreSQL");
    }
}
