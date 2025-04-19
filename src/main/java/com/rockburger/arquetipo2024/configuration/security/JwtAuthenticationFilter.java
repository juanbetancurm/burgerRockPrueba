package com.rockburger.arquetipo2024.configuration.security;

import com.rockburger.arquetipo2024.domain.api.IJwtServicePort;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final IJwtServicePort jwtServicePort;
    private final UserDetailsService userDetailsService;

    // Constructor simplified - removed JwtContextHolder as we're not using it anymore
    public JwtAuthenticationFilter(IJwtServicePort jwtServicePort,
                                   UserDetailsService userDetailsService) {
        this.jwtServicePort = jwtServicePort;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            logger.debug("Processing request to '{}' with JWT: {}", request.getRequestURI(),
                    jwt != null ? "present" : "not present");

            if (jwt != null) {
                // Validate JWT token and get user information
                UserModel user = jwtServicePort.validateAndGetUserFromToken(jwt);
                logger.debug("User authenticated with role: {}", user.getRole());

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                logger.debug("UserDetails loaded with authorities: {}", userDetails.getAuthorities());

                // Store the JWT token in Authentication credentials
                // This allows the token to be retrieved by the Feign interceptor
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                jwt, // Store JWT token here for later retrieval
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Set authentication in SecurityContext with token");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}