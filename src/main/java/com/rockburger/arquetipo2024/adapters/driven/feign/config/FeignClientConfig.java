package com.rockburger.arquetipo2024.adapters.driven.feign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients to properly interact with the cart service.
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public JwtTokenInterceptor jwtTokenInterceptor() {
        return new JwtTokenInterceptor();
    }

}