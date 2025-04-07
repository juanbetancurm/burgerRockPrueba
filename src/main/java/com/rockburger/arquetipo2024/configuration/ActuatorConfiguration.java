package com.rockburger.arquetipo2024.configuration;


import com.rockburger.arquetipo2024.adapters.driven.feign.CartFeignClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Add actuator endpoints for health monitoring
@Configuration
public class ActuatorConfiguration {

    @Bean
    public HealthIndicator cartServiceHealth(CartFeignClient cartFeignClient) {
        return new AbstractHealthIndicator() {
            @Override
            protected void doHealthCheck(Health.Builder builder) {
                try {
                    // Simple health check to the cart service
                    cartFeignClient.healthCheck();
                    builder.up();
                } catch (Exception e) {
                    builder.down().withException(e);
                }
            }
        };
    }
}