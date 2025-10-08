package com.midlane.project_management_tool_api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${USER_SERVICE_URL:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${PROJECT_SERVICE_URL:http://localhost:8083}")
    private String projectServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8084}")
    private String notificationServiceUrl;

    @Value("${COLLAB_SERVICE_URL:http://localhost:8090}")
    private String collabServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes (including auth endpoints)
                .route("user-service", r -> r
                        .path("/api/users/**", "/api/auth/**")
                        .uri(userServiceUrl))

                // Project Service Routes
                .route("project-service", r -> r
                        .path("/api/projects/**")
                        .uri(projectServiceUrl))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri(notificationServiceUrl))

                // Collaboration Service Routes
                .route("collab-service", r -> r
                        .path("/api/collab/**")
                        .uri(collabServiceUrl))

                .build();
    }
}
