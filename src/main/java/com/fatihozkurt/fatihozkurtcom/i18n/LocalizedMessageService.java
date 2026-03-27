package com.fatihozkurt.fatihozkurtcom.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Resolves localized messages from configured message bundles.
 */
@Component
@RequiredArgsConstructor
public class LocalizedMessageService {

    private final MessageSource messageSource;
    private final RequestLocaleService requestLocaleService;

    /**
     * Resolves a localized message by key.
     *
     * @param key i18n key
     * @param args optional args
     * @return resolved message
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, key, requestLocaleService.resolve());
    }
}
