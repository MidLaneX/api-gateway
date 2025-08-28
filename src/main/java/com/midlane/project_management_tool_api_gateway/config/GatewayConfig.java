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

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route all auth service traffic to user service
                .route("auth-service-route", r -> r
                        .path("/api/auth/**")
                        .uri("http://localhost:8082")
                )
                // User service routes (for non-auth endpoints)
                .route("user-service-route", r -> r
                        .path("/api/users/**")
                        .uri("http://localhost:8082")
                )
                // Project service routes
                .route("project-service-route", r -> r
                        .path("/api/projects/**")
                        .uri("http://localhost:8083")
                )
                // Notification service routes
                .route("notification-service-route", r -> r
                        .path("/api/notifications/**")
                        .uri("http://localhost:8084")
                )
                .build();
    }
}
