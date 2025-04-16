package com.rockburger.arquetipo2024.adapters.driven.feign.config;

import com.rockburger.arquetipo2024.configuration.security.JwtContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intercepts Feign requests and adds the JWT token from the thread-local context.
 * This ensures authentication flows correctly from the main application to the cart service.
 */
public class JwtTokenInterceptor implements RequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtContextHolder jwtContextHolder;

    public JwtTokenInterceptor(JwtContextHolder jwtContextHolder) {
        this.jwtContextHolder = jwtContextHolder;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = jwtContextHolder.getToken();
        if (token != null && !token.isEmpty()) {
            logger.debug("Adding JWT token to Feign request to: {}", requestTemplate.url());
            requestTemplate.header(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
        } else {
            logger.warn("No JWT token found in context for Feign request to: {}", requestTemplate.url());
        }
    }
}