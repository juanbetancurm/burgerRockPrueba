package com.rockburger.arquetipo2024.configuration.security;

import org.springframework.stereotype.Component;

/**
 * Holds the JWT token for the current thread.
 * This allows reliable token propagation between the main application
 * and downstream services via Feign clients.
 */
@Component
public class JwtContextHolder {
    private static final ThreadLocal<String> currentToken = new ThreadLocal<>();

    /**
     * Stores the JWT token in thread-local storage
     */
    public void setToken(String token) {
        currentToken.set(token);
    }

    /**
     * Retrieves the JWT token from thread-local storage
     */
    public String getToken() {
        return currentToken.get();
    }

    /**
     * Clears the JWT token from thread-local storage.
     * Should be called after request processing to prevent memory leaks.
     */
    public void clearToken() {
        currentToken.remove();
    }
}