package com.rockburger.arquetipo2024.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityContextCopyingRequestInterceptor securityContextInterceptor;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          SecurityContextCopyingRequestInterceptor securityContextInterceptor) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityContextInterceptor = securityContextInterceptor;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/error").permitAll()
                .antMatchers("/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/api/auth/**").permitAll()
                .antMatchers("/supply/**")
                .hasAnyRole("auxiliar", "admin")
                .antMatchers("/category/**", "/brand/**", "/article/**")
                .hasAnyRole("admin", "auxiliar")
                .antMatchers("/cart/**", "/purchase/**")
                .hasRole("client")
                .anyRequest().authenticated()
                .and()
                // Add the context copying filter BEFORE the JWT filter to ensure context is available
                .addFilterBefore(securityContextInterceptor, JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}