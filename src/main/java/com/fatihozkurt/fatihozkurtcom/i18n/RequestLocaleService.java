package com.fatihozkurt.fatihozkurtcom.i18n;

import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves locale from request headers with fallback to configured default.
 */
@Component
@RequiredArgsConstructor
public class RequestLocaleService {

    private final AppProperties appProperties;

    /**
     * Resolves locale for current request.
     *
     * @return locale
     */
    public Locale resolve() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String configuredDefault = appProperties.getLocale().getDefaultLocale();
        Locale fallback = StringUtils.hasText(configuredDefault) ? Locale.forLanguageTag(configuredDefault) : Locale.ENGLISH;
        if (attributes == null) {
            return fallback;
        }

        String headerName = appProperties.getLocale().getHeaderName();
        String explicit = attributes.getRequest().getHeader(headerName);
        if (StringUtils.hasText(explicit) && isSupported(explicit)) {
            return Locale.forLanguageTag(explicit);
        }

        String acceptLanguage = attributes.getRequest().getHeader("Accept-Language");
        if (StringUtils.hasText(acceptLanguage)) {
            String candidate = acceptLanguage.split(",")[0].trim();
            if (isSupported(candidate)) {
                return Locale.forLanguageTag(candidate);
            }
        }
        return fallback;
    }

    private boolean isSupported(String locale) {
        String normalized = locale.toLowerCase(Locale.ROOT);
        return normalized.startsWith("tr") || normalized.startsWith("en");
    }
}
