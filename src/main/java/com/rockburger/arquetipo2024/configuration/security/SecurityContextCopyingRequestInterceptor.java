package com.rockburger.arquetipo2024.configuration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the SecurityContext for the duration of the request
 * to make it available to background threads.
 */
@Component
public class SecurityContextCopyingRequestInterceptor implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityContextCopyingRequestInterceptor.class);

    // Use ConcurrentHashMap to store request-specific security contexts
    private final ConcurrentHashMap<String, SecurityContext> requestSecurityContexts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Generate a unique request ID
        String requestId = generateRequestId((HttpServletRequest)request);

        try {
            // Store the security context at the beginning of request processing
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication auth = context.getAuthentication();

            if (auth != null) {
                logger.debug("Storing SecurityContext for request: {}", requestId);
                requestSecurityContexts.put(requestId, context);

                // Set thread-local request ID to allow retrieval by other components
                RequestContextHolder.setRequestId(requestId);
            }

            chain.doFilter(request, response);
        } finally {
            // Clean up after request is complete
            requestSecurityContexts.remove(requestId);
            RequestContextHolder.clear();
        }
    }

    /**
     * Returns the SecurityContext for the current request, if available
     */
    public SecurityContext getSecurityContextForCurrentRequest() {
        String requestId = RequestContextHolder.getRequestId();
        if (requestId != null) {
            SecurityContext context = requestSecurityContexts.get(requestId);
            if (context != null) {
                logger.debug("Retrieved SecurityContext for request: {}", requestId);
                return context;
            }
        }
        return null;
    }

    private String generateRequestId(HttpServletRequest request) {
        return request.getSession().getId() + "-" + System.nanoTime();
    }
}