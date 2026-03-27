package com.fatihozkurt.fatihozkurtcom.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Forces CSRF token generation so SPA clients can read the cookie value.
 */
@Component
public class CsrfCookieFilter extends OncePerRequestFilter {

    /**
     * Generates token by touching request attribute.
     *
     * @param request request
     * @param response response
     * @param filterChain chain
     * @throws ServletException servlet error
     * @throws IOException io error
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
