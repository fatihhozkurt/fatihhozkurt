package com.fatihozkurt.fatihozkurtcom.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves request metadata such as ip and user agent.
 */
@Component
public class RequestMetadataService {

    /**
     * Resolves client ip with proxy header fallback.
     *
     * @param request request
     * @return ip address
     */
    public String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Resolves user agent.
     *
     * @param request request
     * @return user agent
     */
    public String resolveUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "unknown";
    }
}
