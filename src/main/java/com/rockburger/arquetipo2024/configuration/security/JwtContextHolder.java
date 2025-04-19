package com.rockburger.arquetipo2024.configuration.security;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskDecorator;

/**
 * Enhanced JWT token holder that propagates JWT context between threads.
 * Uses InheritableThreadLocal to better support thread pools.
 */
@Component
public class JwtContextHolder {
    private static final Logger logger = LoggerFactory.getLogger(JwtContextHolder.class);

    // Using InheritableThreadLocal to propagate to child threads
    private static final InheritableThreadLocal<String> currentToken = new InheritableThreadLocal<>();

    /**
     * Stores the JWT token in thread-local storage
     */
    public void setToken(String token) {
        logger.debug("Setting JWT token in context holder");
        currentToken.set(token);
    }

    /**
     * Retrieves the JWT token from thread-local storage
     */
    public String getToken() {
        String token = currentToken.get();
        logger.debug("Getting JWT token from context holder: {}", token != null ? "present" : "not present");
        return token;
    }

    /**
     * Clears the JWT token from thread-local storage.
     */
    public void clearToken() {
        logger.debug("Clearing JWT token from context holder");
        currentToken.remove();
    }

    /**
     * Creates a task decorator that propagates JWT context to worker threads
     */
    public TaskDecorator createContextPropagationDecorator() {
        return runnable -> {
            String token = getToken();
            return () -> {
                String originalToken = currentToken.get();
                try {
                    if (token != null) {
                        setToken(token);
                    }
                    runnable.run();
                } finally {
                    if (originalToken != null) {
                        setToken(originalToken);
                    } else {
                        clearToken();
                    }
                }
            };
        };
    }
}