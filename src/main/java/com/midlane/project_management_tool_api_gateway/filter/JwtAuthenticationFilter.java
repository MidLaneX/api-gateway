package com.midlane.project_management_tool_api_gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.midlane.project_management_tool_api_gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Filter for API Gateway
 * - Auth endpoints (/auth/**) are forwarded directly to auth service without verification
 * - All other endpoints require valid JWT token
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // Public endpoints that don't require JWT verification
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/auth/",
        "/actuator/health",
        "/actuator/info"
    );

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().toString();

            logger.info("Processing request: {} {}", method, path);

            // Skip JWT verification for public paths (auth service, health checks)
            if (isPublicPath(path)) {
                logger.debug("Public path accessed, skipping JWT verification: {}", path);
                return chain.filter(exchange);
            }

            // For all other paths, JWT verification is required
            try {
                // Check if Authorization header is present
                if (isAuthMissing(request)) {
                    logger.warn("Authorization header missing for protected path: {}", path);
                    return handleAuthError(exchange, "Authorization header is missing", "MISSING_AUTH_HEADER");
                }

                // Extract JWT token from Authorization header
                final String token = getAuthHeader(request);

                if (token == null || token.trim().isEmpty()) {
                    logger.warn("Invalid Authorization header format for path: {}", path);
                    return handleAuthError(exchange, "Invalid Authorization header format", "INVALID_AUTH_HEADER");
                }

                // Validate JWT token
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("JWT token validation failed for path: {}", path);
                    return handleAuthError(exchange, "Invalid or expired JWT token", "INVALID_TOKEN");
                }

                // Extract user information and enrich request headers for downstream services
                try {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);
                    logger.info("JWT validation successful for user: {} with role: {} accessing path: {}", username, role, path);

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", username)
                            .header("X-User-Name", username)
                            .header("X-User-Role", role != null ? role : "USER")
                            .header("X-Request-Source", "api-gateway")
                            .header("X-Request-Time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .header("X-Token-Type", "ACCESS")
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    logger.error("Error extracting user information from JWT token", e);
                    return handleAuthError(exchange, "Error processing JWT token", "TOKEN_PROCESSING_ERROR");
                }

            } catch (Exception e) {
                logger.error("Unexpected error during JWT authentication for path: {}", path, e);
                return handleAuthError(exchange, "Authentication service error", "AUTH_SERVICE_ERROR");
            }
        };
    }

    /**
     * Check if the requested path is public and doesn't require JWT verification
     */
    private boolean isPublicPath(String path) {
        // Check for exact matches first
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }

        // Check for path prefixes (especially for auth endpoints)
        return path.startsWith("/api/auth/") ||
               path.equals("/actuator/health") ||
               path.equals("/actuator/info");
    }

    /**
     * Check if Authorization header is missing
     */
    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getAuthHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Handle authentication errors with structured JSON response
     */
    private Mono<Void> handleAuthError(ServerWebExchange exchange, String message, String errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // Add security headers
        response.getHeaders().add("X-Content-Type-Options", "nosniff");
        response.getHeaders().add("X-Frame-Options", "DENY");

        // Create structured error response
        ErrorResponse errorResponse = new ErrorResponse(
            message,
            errorCode,
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            exchange.getRequest().getURI().getPath()
        );

        try {
            String body = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
            return response.writeWith(Flux.just(buffer));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing error response", e);
            // Fallback to simple error response
            String fallbackBody = String.format(
                "{\"error\":\"%s\",\"errorCode\":\"%s\",\"status\":%d}",
                message, errorCode, HttpStatus.UNAUTHORIZED.value()
            );
            DataBuffer buffer = response.bufferFactory().wrap(fallbackBody.getBytes());
            return response.writeWith(Flux.just(buffer));
        }
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        // Configuration properties can be added here if needed
    }

    /**
     * Error response structure for consistent API responses
     */
    private static class ErrorResponse {
        private final String message;
        private final String errorCode;
        private final int status;
        private final String statusText;
        private final String timestamp;
        private final String path;

        public ErrorResponse(String message, String errorCode, int status,
                           String statusText, String timestamp, String path) {
            this.message = message;
            this.errorCode = errorCode;
            this.status = status;
            this.statusText = statusText;
            this.timestamp = timestamp;
            this.path = path;
        }

        // Getters for JSON serialization
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
        public int getStatus() { return status; }
        public String getStatusText() { return statusText; }
        public String getTimestamp() { return timestamp; }
        public String getPath() { return path; }
    }
}
