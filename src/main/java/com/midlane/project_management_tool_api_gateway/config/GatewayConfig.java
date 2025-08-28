package com.midlane.project_management_tool_api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class GatewayConfig {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${USER_SERVICE_URL:http://user-service:8082}")
    private String userServiceUrl;

    @Value("${PROJECT_SERVICE_URL:http://project-service:8083}")
    private String projectServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://notification-service:8085}")
    private String notificationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route all auth service traffic to user service
                .route("auth-service-route", r -> r
                        .path("/api/auth/**")
                        .uri(userServiceUrl)
                )
                // User service routes (for non-auth endpoints)
                .route("user-service-route", r -> r
                        .path("/api/users/**")
                        .uri(userServiceUrl)
                )
                // Project service routes
                .route("project-service-route", r -> r
                        .path("/api/projects/**")
                        .uri(projectServiceUrl)
                )
                // Notification service routes
                .route("notification-service-route", r -> r
                        .path("/api/notifications/**")
                        .uri(notificationServiceUrl)
                )
                .build();
    }
}
