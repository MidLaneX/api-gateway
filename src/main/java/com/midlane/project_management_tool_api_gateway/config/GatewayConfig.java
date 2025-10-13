package com.midlane.project_management_tool_api_gateway.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfig {

    @Value("${USER_SERVICE_URL:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${PROJECT_SERVICE_URL:http://localhost:8083}")
    private String projectServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8084}")
    private String notificationServiceUrl;

    @Value("${COLLAB_SERVICE_URL:http://localhost:8090}")
    private String collabServiceUrl;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow all origins
        corsConfig.addAllowedOriginPattern("*");
        
        // Allow all HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow all headers
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);
        
        // Expose authorization header
        corsConfig.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Cache preflight response for 1 hour
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

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
                        .filters(f -> f.rewritePath("/api/collab/(?<segment>.*)", "/api/${segment}"))
                        .uri(collabServiceUrl))

                .build();
    }
}
