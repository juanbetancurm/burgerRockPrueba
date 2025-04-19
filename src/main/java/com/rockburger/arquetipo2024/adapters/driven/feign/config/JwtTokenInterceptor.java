package com.rockburger.arquetipo2024.adapters.driven.feign.config;

import com.rockburger.arquetipo2024.configuration.security.SecurityContextCopyingRequestInterceptor;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Intercepts Feign client requests to add the JWT token from the stored SecurityContext.
 */
public class JwtTokenInterceptor implements RequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecurityContextCopyingRequestInterceptor securityContextInterceptor;

    public JwtTokenInterceptor(SecurityContextCopyingRequestInterceptor securityContextInterceptor) {
        this.securityContextInterceptor = securityContextInterceptor;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // First try to get token from current thread's SecurityContext
        String token = getTokenFromCurrentSecurityContext();

        // If not found, try to get from the stored request context
        if (token == null) {
            token = getTokenFromStoredSecurityContext();
        }

        if (token != null && !token.isEmpty()) {
            logger.debug("Adding JWT token to Feign request to: {}", requestTemplate.url());
            requestTemplate.header(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
        } else {
            logger.warn("No JWT token found for Feign request to: {}", requestTemplate.url());
        }
    }

    private String getTokenFromCurrentSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }

    private String getTokenFromStoredSecurityContext() {
        SecurityContext storedContext = securityContextInterceptor.getSecurityContextForCurrentRequest();
        if (storedContext != null && storedContext.getAuthentication() != null &&
                storedContext.getAuthentication().getCredentials() instanceof String) {
            return (String) storedContext.getAuthentication().getCredentials();
        }
        return null;
    }
}