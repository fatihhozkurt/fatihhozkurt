# Fatihozkurt.com Business Analysis Document

## 1. Document Purpose

This document defines the business scope, functional requirements, non-functional requirements, security expectations, system boundaries, data ownership, and delivery plan for `fatihozkurt.com`.

The target product is:

- A public one-page personal portfolio website for visitors and recruiters
- A secure admin interface for managing all public content and monitoring operational/security events
- A backend-centric architecture based on Java, Spring Boot, PostgreSQL, MinIO, Docker, and a React + Tailwind frontend

## 2. Product Vision

The platform should present Fatih Ozkurt as a professional Java backend developer through a polished, modern, responsive, and content-manageable website.

The product has two faces:

- Public surface: portfolio, projects, Medium articles, CV, contact options, and contact form
- Private surface: authenticated admin panel to manage content, files, analytics, and security-sensitive operations

## 3. Business Goals

- Increase credibility during job applications
- Provide a single authoritative personal brand website
- Showcase technical depth, projects, written content, and CV
- Allow non-developer-style content updates without code changes
- Track visitor activity and suspicious access attempts
- Maintain a strong security baseline suitable for an internet-facing portfolio and admin panel

## 4. Scope

### 4.1 In Scope

- Responsive one-page public site
- Dark + pastel visual language
- Animated hero/landing experience
- Sticky header with logo and section navigation
- Sections for hero, about/intro, tech stack, projects, Medium articles, CV, contact
- Project detail modal/drawer with rendered README content
- Medium article cards with outbound navigation
- Embedded PDF CV viewer with download action
- Contact form sending email to configurable inbox
- Admin authentication and session management
- Password reset via email
- Admin content management for all major public sections
- Analytics dashboard and security event visibility
- REST API backend
- Dockerized runtime
- PostgreSQL for relational data
- MinIO for media/documents
- Redis if rate limiting, caching, or token/session support requires it
- OpenSearch for logs/searchable operational analytics

### 4.2 Out of Scope for Initial Release

- Multi-admin / role hierarchy unless explicitly requested later
- Public user accounts
- Blog engine hosted directly on the site
- Multilingual support
- Advanced marketing automation
- Payment flows
- Native mobile app

## 5. Key Users

### 5.1 Visitor

Any recruiter, hiring manager, engineer, or guest browsing the public site.

Primary goals:

- Understand who Fatih Ozkurt is
- Review experience, tech stack, and projects
- Read articles
- View/download CV
- Reach out directly

### 5.2 Site Owner / Admin

Fatih Ozkurt as the sole administrator.

Primary goals:

- Securely sign in
- Update public content without deployments
- Manage projects, articles, tech stack, contact data, and CV
- Monitor visits and attack signals
- Review security and system logs

## 6. UX and Branding Requirements

### 6.1 Visual Direction

- Dark-first, pastel-accented interface
- Premium, intentional, modern feel
- Responsive across desktop, tablet, and mobile
- Smooth one-page scrolling navigation
- Animated but restrained entrance sequence on first load

### 6.2 Recommended Hero Content

- Full name
- Professional title
- Short welcome sentence
- Primary CTA
- Secondary CTA

Example structure:

- Welcome text
- `Fatih Ozkurt`
- `Java Backend Developer`
- Short 1-2 line positioning statement

### 6.3 Recommended Animation Direction

The user has not fixed the animation style yet. The recommended direction is:

- Layered reveal animation on initial hero load
- Subtle motion background or gradient mesh
- Text stagger animation for greeting, name, and title
- Smooth section transitions instead of heavy motion effects

Reason:

- Feels modern without hurting readability or performance
- Works better for recruiters than high-noise cinematic intros

## 7. Public Website Functional Requirements

### 7.1 Header and Navigation

- Persistent header on the page
- Logo on the left
- Navigation centered or right-aligned
- Clicking navigation items scrolls to the relevant section anchor
- Active section state should be visually highlighted
- Mobile navigation should collapse into a secure and accessible menu

### 7.2 Hero Section

- Animated entry
- Name, surname, title, and welcome text
- Optional CTA buttons such as `Projects`, `Contact`, `Download CV`
- Hero content editable from admin panel

### 7.3 Introduction / About Section

- Short professional summary
- Optional highlights such as years of experience, domain interests, and core strengths
- Editable from admin panel

### 7.4 Tech Stack Section

The user suggested either a floating ticker-like display or an alternative. Recommended implementation:

- A polished horizontal skill marquee or drifting chip cloud
- Each item includes icon + name
- Group skills by category if needed: backend, cloud/devops, database, tooling

Why this is preferred:

- Keeps the page dynamic without distracting from project content
- Matches one-page visual rhythm better than a static icon grid alone

Admin capabilities:

- Add/edit/remove tech items
- Upload/select icon
- Set display order
- Set category and highlight flag

### 7.5 Projects Section

- Card-based list
- Each card includes cover image, title, short summary, and GitHub link
- Clicking the card opens a project detail modal or full-screen overlay

Project detail contents:

- Large image/banner
- Project title
- Project summary
- Tech stack with icons
- GitHub link with icon
- Optional live/demo link
- Rendered README content from Markdown

Admin capabilities:

- Create/update/delete project
- Upload cover image(s)
- Set project order
- Manage GitHub URL
- Manage Markdown/README content
- Manage per-project tech stack
- Toggle visibility / featured flag

### 7.6 Medium Articles Section

- Card-based article list
- Each card includes cover image, title, excerpt, publish date, and Medium link
- Clicking a card opens the Medium article in a new tab

Admin capabilities:

- Create/update/delete article cards
- Store canonical Medium URL
- Optional manual sync fields for image, title, excerpt, publish date

### 7.7 CV Section

- Centered PDF viewer
- Download button above the viewer
- Fallback link if PDF cannot be embedded on the browser

Admin capabilities:

- Replace current CV PDF
- Store file in object storage
- Keep metadata such as filename, version date, and size

### 7.8 Contact Section

- Contact cards for email, LinkedIn, GitHub, Medium
- Contact form fields: `title`, `email`, `content`
- Submission should send an email to the configured owner mailbox

Recommended additions:

- Optional spam protection
- Submission success/failure feedback
- Throttling per IP and per email

Admin capabilities:

- Update public contact links
- Update recipient mailbox address
- View contact form submission logs or delivery status

## 8. Admin Interface Functional Requirements

## 8.1 Entry Point

- Entry path: `/auth`
- The route should show a secure login screen
- After successful authentication, the user enters the admin panel

Recommended routing split:

- `/auth` or `/auth/login`: login screen
- `/auth/forgot-password`: reset request
- `/auth/reset-password`: reset completion
- `/admin`: authenticated admin shell

This keeps authentication and administration concerns separate while preserving the desired `/auth` entry point.

### 8.2 Authentication

- Username + password sign-in
- Password complexity validation in both frontend and backend
- Minimum password policy:
  - at least 8 characters
  - at least 1 uppercase letter
  - at least 1 lowercase letter
  - at least 1 digit
  - at least 1 special character

Recommended production baseline:

- Minimum 12 characters instead of 8
- Argon2id or BCrypt with strong work factor
- Generic login error messages
- Account/IP rate limiting
- Audit logging of successful and failed attempts

### 8.3 Forgot Password

- `Forgot password` action on login screen
- Reset request sent to the admin email stored in the system
- Email template must match site branding
- Reset link must be single-use, time-limited, and signed
- Reset form must enforce password policy again
- All reset actions logged

### 8.4 Logout and Session Security

- Explicit logout action
- Refresh token support
- CSRF protection for cookie-based authenticated flows
- Token/session rotation
- Session invalidation on logout and password reset

Recommended implementation:

- Short-lived access token
- Rotating refresh token
- HttpOnly, Secure, SameSite cookies
- CSRF token for state-changing requests

### 8.5 Admin Dashboard

The admin panel should provide at least:

- Total visit count
- Unique visitor count
- Page/section popularity
- Recent visits
- IP-based request activity
- Country distribution
- Failed login attempts
- Password reset attempts
- Contact form submission metrics
- Recent important logs
- Security event indicators

Dashboard widgets/charts may include:

- Visits over time
- Top countries
- Top referrers
- Failed login trend
- Recent suspicious IPs

### 8.6 Content Management

The admin panel must allow editing of:

- Hero texts
- About section content
- Tech stack items
- Project cards and project details
- Medium article cards
- CV file
- Contact links
- Contact recipient mailbox
- SEO metadata
- Social links

Recommended content model:

- Use structured CMS-like forms instead of one giant text editor
- Keep ordering, visibility, and publish status controllable per content type

## 9. Security Requirements

## 9.1 Security Positioning

`Non-hackable` is not a realistic engineering guarantee. The practical target is:

- defense in depth
- minimized attack surface
- strong authentication/session controls
- secure defaults
- high visibility into abuse and incidents

### 9.2 Mandatory Security Controls

- Spring Security hardened configuration
- Strict input validation
- DTO-based request/response contracts
- Bean Validation support for create/update request models
- Custom validation support for partial update payloads
- Output encoding where relevant
- CSRF protection
- Rate limiting for login, reset, and contact form
- Account enumeration resistance
- Secure password hashing
- Token rotation and revocation
- Security headers
- CORS restricted to known origins
- Swagger/OpenAPI disabled or strictly protected in production
- Secrets outside source control
- Encrypted transport via HTTPS only
- Audit logging
- File type and size validation for uploads
- Antivirus or content validation consideration for uploaded files
- MinIO bucket policy hardening
- Principle of least privilege

### 9.3 Additional Recommended Controls

- Reverse proxy with WAF/rate-limit layer
- IP allowlist for admin if operationally acceptable
- Bot mitigation for contact form
- Access anomaly detection
- Geo/IP enrichment only after privacy review
- Dependency vulnerability scanning
- SAST/DAST in CI
- Structured incident logs to OpenSearch

### 9.4 Swagger Requirement

User requirement: Swagger should not be exposed publicly.

Accepted implementation options:

- Disable Swagger entirely in production
- Expose Swagger only in local/dev environments
- Or protect it behind VPN/IP allowlist/basic auth on non-public environments

Recommendation:

- No Swagger UI on public production at all

## 10. Analytics and Logging Requirements

### 10.1 Visitor Analytics

Track:

- Request timestamp
- Path/section
- IP address or privacy-safe derivative
- Country
- User agent
- Referrer
- Session identifier

### 10.2 Security Analytics

Track:

- Failed logins
- Rate-limit hits
- Reset requests
- Reset completions
- Suspicious repeated requests
- Admin actions
- File uploads/replacements

### 10.3 Log Visibility

Admin should be able to view:

- Important recent events
- Filtered event lists
- Severity levels
- Searchable logs

### 10.4 Privacy and Compliance

Because IP and country data are personal-data adjacent, the system should account for privacy compliance concerns such as KVKK/GDPR-like obligations.

Recommendation:

- Document retention policy
- Consider masking or hashing IP where full raw IP is not operationally required
- Add privacy notice content to the public site

## 11. Architecture Requirements

### 11.1 Technology Stack

- Backend: Java + Spring Boot
- Frontend: React + Tailwind CSS
- Database: PostgreSQL
- Object Storage: MinIO
- Cache / rate-limit / token support: Redis
- Searchable logs/analytics: OpenSearch
- Containerization: Docker
- API style: REST

### 11.2 Logical Components

- Public frontend application
- Admin frontend application
- Spring Boot API
- PostgreSQL database
- MinIO object storage
- Redis
- OpenSearch
- Email service integration
- Reverse proxy / ingress

### 11.3 Environment and Profile Strategy

The backend must support at least two Spring profiles:

- `local`
- `product`

Requirements:

- `local` profile for developer machine defaults and local integrations
- `product` profile for deployment/runtime configuration
- Deployment-time secrets and runtime values must be injectable through `.env`
- Docker Compose should provide the runtime wiring for environment variables and dependent services
- Sensitive values must not be hardcoded in source-controlled configuration files

Recommended configuration structure:

- `application.yaml`
- `application-local.yaml`
- `application-product.yaml`
- `.env` for deployment-supplied variables
- `compose.yaml` or `docker-compose.yml` for service orchestration

### 11.4 Suggested High-Level Module Breakdown

- `identity-access`
- `content-management`
- `project-management`
- `article-management`
- `asset-management`
- `cv-management`
- `contact-messaging`
- `analytics-observability`
- `security-audit`

### 11.5 Frontend Structure Recommendation

Since the user asked to open the frontend in the same parent directory:

- `fatihozkurtcom/` -> Spring Boot backend
- `fatihozkurtcom-frontend/` or `frontend/` -> React app

Recommendation:

- Use a separate frontend project in the same workspace for cleaner delivery and deployment

## 12. Data Domains and Suggested Entity Model

### 12.1 Core Entities

- `AdminUser`
- `PasswordResetToken`
- `RefreshTokenSession`
- `HeroContent`
- `AboutContent`
- `TechStackItem`
- `Project`
- `ProjectAsset`
- `ProjectTechStackItem`
- `MediumArticle`
- `CvDocument`
- `ContactProfile`
- `ContactMessage`
- `SiteSetting`
- `VisitEvent`
- `SecurityEvent`
- `AdminAuditEvent`

### 12.2 Important Entity Notes

- Public content entities should support sort order and active/inactive status
- Media files should store MinIO object key, original filename, content type, checksum, and size
- Sensitive auth/session data should support expiry, revocation, and traceability

## 13. API Requirement Groups

### 13.1 Public APIs

- Fetch landing/hero content
- Fetch about content
- Fetch tech stack
- Fetch projects and project detail
- Fetch Medium cards
- Fetch CV metadata/download URL
- Fetch contact profile
- Submit contact form

### 13.2 Admin Auth APIs

- Login
- Logout
- Refresh token
- Forgot password request
- Reset password completion
- CSRF token retrieval if required by chosen pattern

### 13.3 Admin Content APIs

- Manage hero content
- Manage about section
- Manage tech stack
- Manage projects
- Manage Medium cards
- Manage CV document
- Manage contact profile
- Manage site settings

API contract rule:

- Create requests should enforce full validation on mandatory fields
- Update requests should support partial payloads on `PUT` endpoints where omitted fields are ignored
- If a field is present in an update payload, it must be validated with field-specific constraints such as `not empty`, `length`, `format`, `pattern`, and domain-specific rules
- This behavior should be implemented through reusable custom validation annotations/components rather than duplicated controller/service logic
- `PATCH` is out of scope for the current API style unless added in a future phase

### 13.4 Admin Observability APIs

- Dashboard summary
- Visit analytics
- Security event list
- Audit event list
- Contact submission report

## 14. Non-Functional Requirements

### 14.1 Performance

- Fast first meaningful paint on public site
- Efficient image delivery and optimization
- Pagination for admin event lists
- Avoid heavy animation blocking the initial load

### 14.2 Reliability

- Graceful degradation if analytics backend is delayed
- File upload safety and recovery handling
- Reset token expiry enforcement
- Backups for PostgreSQL and MinIO assets

### 14.3 Maintainability

- Clear layered architecture
- DTO usage across API boundaries
- Design patterns where justified, not decorative
- Javadoc on classes and methods as requested
- Environment-specific configuration separation
- Reusable validation infrastructure for create/update flows
- Centralized exception model and error-code catalog

### 14.4 Accessibility

- Keyboard navigable sections and modals
- Focus states
- Sufficient color contrast despite dark + pastel palette
- Semantic markup and labels

### 14.5 SEO

- Metadata per page/section
- Open Graph support
- Structured headings
- Fast load and crawlable core content

## 15. Content Management Rules

Recommended operational rules:

- Every editable content type should support validation
- Admin changes should be auditable
- Destructive actions should require confirmation
- Important files should support version replacement without orphan leaks
- Markdown content should be sanitized before rendering

## 16. Validation, Error Handling, and Localization Requirements

### 16.1 Request Validation Strategy

The system must validate incoming REST payloads consistently.

Requirements:

- Standard Bean Validation annotations should be used where sufficient
- For update endpoints, partial update semantics must be supported on `PUT` requests
- A custom validation annotation/mechanism must exist to validate only the fields that are present in the request body
- If a field is omitted in an update request, validation for that field should not run
- If a field is provided, it must be validated against the relevant constraints

Example intent:

- `null` because the client did not send the field: skip validation
- empty string sent for a field that must not be blank: fail validation
- malformed email/URL/phone/date sent in an optional update field: fail validation

Recommended implementation direction:

- Introduce reusable custom annotations such as `validate-if-present` style constraints
- Keep validation rules declarative at DTO level where possible
- Ensure the same constraints are not fragmented across controller and service layers

### 16.2 Exception Model

The backend must use a centralized exception handling model.

Requirements:

- Exceptions must map to stable business/system error codes
- Error codes should follow a readable convention such as `USR001`, `AUTH001`, `VAL001`, `SYS001`
- API error responses should include at least:
  - error code
  - localized message
  - technical timestamp
  - request path
  - optional field validation details

Recommended categories:

- `USRxxx` for user/content/domain errors
- `AUTHxxx` for authentication/authorization errors
- `VALxxx` for validation errors
- `SECxxx` for security/rate-limit/csrf events
- `SYSxxx` for unexpected internal errors

### 16.3 Localized Exception Messages

Exception messages must be externalized and localized.

Requirements:

- Keep Turkish and English messages in separate resource files
- Selected language must be controlled by an application configuration value sourced from `application.yaml` and overridable by environment/profile configuration
- The design must also support per-request language selection via header for future/current use
- The error code should resolve to the correct localized message template

Recommended resource structure:

- `messages_tr.properties`
- `messages_en.properties`
- Optional dedicated error catalogs such as:
  - `errors_tr.properties`
  - `errors_en.properties`

Recommended configuration behavior:

- `app.locale=tr` or `app.locale=en`
- Profile- and environment-specific override support
- Request-level override support via locale header such as `Accept-Language` or a dedicated application header
- Fallback to configured default locale when request-level locale is absent or unsupported

### 16.4 Logging Requirements for Validation and Exceptions

Requirements:

- Validation failures must be logged at appropriate level without leaking secrets
- Security-sensitive failures such as login, reset, csrf, and rate-limit events must be logged with audit value
- Unexpected exceptions must include correlation/tracing context where available
- Logs must avoid exposing passwords, reset tokens, session tokens, or sensitive personal content

Recommended logging policy:

- business validation: `WARN`
- suspicious/security events: `WARN` or `INFO` to audit stream depending on volume
- unexpected system failures: `ERROR`

## 17. Email Requirements

Two email flows are mandatory:

- Contact form delivery email
- Password reset email

Recommended email requirements:

- Branded HTML template
- Plain-text fallback
- Link expiration text
- Anti-abuse throttling
- Delivery status logging

## 18. Risks and Constraints

### 17.1 Risks

- `Absolute security` expectation is unattainable; risk can only be reduced, not eliminated
- Analytics with raw IP storage may create compliance and retention obligations
- README rendering may allow XSS if Markdown sanitization is weak
- Public contact forms are spam targets
- Reset-email delivery depends on third-party SMTP/provider reliability
- OpenSearch may be operationally heavy for a small personal site unless scoped carefully

### 17.2 Constraint Notes

- The project should remain maintainable by a single engineer
- Feature richness should not create unnecessary hosting/operations cost
- Security controls must not destroy admin usability

## 19. Recommended Delivery Phases

### Phase 1: Foundation

- Finalize requirements
- Set architecture and environments
- Establish Docker setup
- Create backend skeleton
- Create frontend skeleton
- Configure PostgreSQL, MinIO, Redis
- Define profile/configuration strategy for `local` and `product`
- Define error-code catalog and localization resource structure

### Phase 2: Public Experience

- Build public one-page frontend
- Build public content APIs
- Implement hero, about, tech stack, projects, Medium, CV, contact sections
- Implement media/document storage

### Phase 3: Identity and Security

- Implement admin auth
- Implement password reset
- Implement rate limiting
- Implement CSRF/session security
- Hide Swagger in production
- Implement centralized exception handling and localized error responses
- Implement reusable custom validation for partial update requests

### Phase 4: Admin CMS

- Build admin panel shell
- Add forms for all editable content
- Add file replacement/upload workflows
- Add audit logging

### Phase 5: Analytics and Operations

- Add visit tracking
- Add security event tracking
- Add dashboard charts/tables
- Integrate OpenSearch where justified

### Phase 6: Hardening and Release

- Security review
- Test coverage improvements
- Performance tuning
- Backup/recovery verification
- Deployment and monitoring

## 20. Acceptance Criteria Summary

The initial release is considered successful when:

- Public site is responsive, polished, and fully navigable as a one-page experience
- All required public sections are visible and manageable
- Project and Medium cards work correctly
- CV is viewable and downloadable
- Contact form sends email successfully
- Admin login, logout, refresh token, CSRF, and password reset work securely
- Rate limiting works on abuse-prone endpoints
- Admin can manage all critical website content
- Dashboard shows meaningful visit and security insights
- Swagger is not publicly exposed in production
- Update endpoints validate only supplied fields and reject invalid supplied values
- Error responses return stable error codes and localized messages
- Runtime configuration can be supplied through `.env` with Docker Compose for deployment

## 21. Open Decisions Requiring Clarification

These items are not blockers for analysis, but they must be finalized before implementation starts:

1. Will there be exactly one admin account, or should the design allow future multi-admin support?
2. Which email provider will be used for sending contact and reset emails: SMTP, AWS SES, Resend, SendGrid, Mailgun, or another provider?
3. Should analytics store raw IP addresses, masked IP addresses, or hashed IP fingerprints?
4. Should Medium article cards be fully manual, or should there be an automated sync/import job?
5. Will project README content be authored directly in admin, synced from GitHub, or both?
6. Should the public site support Turkish only, English only, or both?
7. Is the admin panel allowed to be further restricted by IP/VPN, or must it remain globally reachable with application-layer security only?
8. Should contact form spam protection include CAPTCHA/hCaptcha/Cloudflare Turnstile from day one?
9. Is OpenSearch mandatory for MVP, or can analytics start in PostgreSQL and move to OpenSearch in a later phase?
10. What are the deployment targets: VPS, Docker Compose on a server, Kubernetes, or a managed platform?
## 22. Recommendation Summary

Recommended implementation choices for the cleanest first release:

- One admin account for MVP, but model the auth domain for future expansion
- React public/admin frontend in a separate project under the same workspace
- Structured CMS data model in PostgreSQL, files in MinIO
- Redis-backed rate limiting and token/session support
- OpenSearch either scoped to security/event search or postponed if operational cost is too high
- No public Swagger in production
- Strong password policy with 12+ characters in production
- Markdown sanitization for any rendered README content
- Privacy-conscious analytics retention policy
- `local` and `product` Spring profiles with environment-driven secrets
- Centralized error-code based exception handling with Turkish/English message bundles
- Reusable custom validation annotations for partial update DTOs
- Default locale from configuration with request-header-based `tr/en` override support
