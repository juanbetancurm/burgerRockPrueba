package com.rockburger.arquetipo2024.adapters.driven.feign.config;

import com.rockburger.arquetipo2024.configuration.security.JwtContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configuration for Feign clients to properly interact with the cart service.
 */
@Configuration
public class FeignClientConfig {
    private final JwtContextHolder jwtContextHolder;

    public FeignClientConfig(@Lazy JwtContextHolder jwtContextHolder) {
        this.jwtContextHolder = jwtContextHolder;
    }

    @Bean
    public JwtTokenInterceptor jwtTokenInterceptor() {
        return new JwtTokenInterceptor(jwtContextHolder);
    }
}