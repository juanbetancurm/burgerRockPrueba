package com.rockburger.arquetipo2024.adapters.driven.feign.config;

import com.rockburger.arquetipo2024.configuration.security.SecurityContextCopyingRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    private final SecurityContextCopyingRequestInterceptor securityContextInterceptor;

    public FeignClientConfig(SecurityContextCopyingRequestInterceptor securityContextInterceptor) {
        this.securityContextInterceptor = securityContextInterceptor;
    }

    @Bean
    public JwtTokenInterceptor jwtTokenInterceptor() {
        return new JwtTokenInterceptor(securityContextInterceptor);
    }
}