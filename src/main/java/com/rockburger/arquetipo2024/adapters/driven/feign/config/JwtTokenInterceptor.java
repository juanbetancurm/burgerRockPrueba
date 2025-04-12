package com.rockburger.arquetipo2024.adapters.driven.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Intercepts Feign requests and adds the JWT token from the current security context.
 * This ensures that the authorization flows from the main application to the cart service.
 */
public class JwtTokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() instanceof String) {
            String token = (String) authentication.getCredentials();
            requestTemplate.header("Authorization", "Bearer " + token);
        }
    }
}