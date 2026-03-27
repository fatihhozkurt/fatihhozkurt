package com.fatihozkurt.fatihozkurtcom.security.config;
import com.fatihozkurt.fatihozkurtcom.common.api.ApiErrorResponse;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.i18n.LocalizedMessageService;
import com.fatihozkurt.fatihozkurtcom.security.jwt.JwtAuthenticationFilter;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configures Spring Security policies and authentication filters.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties appProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CsrfCookieFilter csrfCookieFilter;
    private final LocalizedMessageService localizedMessageService;

    /**
     * Builds security filter chain.
     *
     * @param http security builder
     * @return configured chain
     * @throws Exception when configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = csrfTokenRepository();
        http
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .ignoringRequestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/public/**"
                        )
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers -> {
                    headers.frameOptions(frame -> frame.deny());
                    headers.contentTypeOptions(Customizer.withDefaults());
                    headers.referrerPolicy(policy -> policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                    headers.permissionsPolicy(permissions -> permissions.policy("camera=(), microphone=(), geolocation=(), payment=()"));
                    headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'"));
                    if (appProperties.getSecurity().isRequireHttps()) {
                        headers.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(31536000));
                    }
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/public/contact-messages").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/public/visits").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(ErrorCode.AUTH002.status().value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ApiErrorResponse payload = new ApiErrorResponse(
                                    ErrorCode.AUTH002.name(),
                                    localizedMessageService.get(ErrorCode.AUTH002.messageKey()),
                                    request.getRequestURI(),
                                    OffsetDateTime.now(),
                                    List.of()
                            );
                            response.getWriter().write(toJson(payload));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(ErrorCode.SEC002.status().value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ApiErrorResponse payload = new ApiErrorResponse(
                                    ErrorCode.SEC002.name(),
                                    localizedMessageService.get(ErrorCode.SEC002.messageKey()),
                                    request.getRequestURI(),
                                    OffsetDateTime.now(),
                                    List.of()
                            );
                            response.getWriter().write(toJson(payload));
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(csrfCookieFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Configures CORS origin policy.
     *
     * @return source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(appProperties.getSecurity().getCors().getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Locale",
                "X-CSRF-TOKEN"
        ));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Provides password encoder bean.
     *
     * @return password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private String toJson(ApiErrorResponse payload) {
        return "{\"code\":\"" + escape(payload.code()) + "\","
                + "\"message\":\"" + escape(payload.message()) + "\","
                + "\"path\":\"" + escape(payload.path()) + "\","
                + "\"timestamp\":\"" + payload.timestamp() + "\","
                + "\"fieldErrors\":[]}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieName("XSRF-TOKEN");
        csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");
        csrfTokenRepository.setCookieCustomizer(builder -> builder
                .secure(appProperties.getSecurity().getCookies().isSecure())
                .sameSite(appProperties.getSecurity().getCookies().getSameSite())
                .path("/"));
        return csrfTokenRepository;
    }
}
