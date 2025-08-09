package com.midlane.project_management_tool_api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class GatewayConfig {

    @Value("${cors.allowed.origins:http://localhost:5173}")
    private String allowedOrigins;

    // Removed the custom CorsWebFilter bean to prevent conflicts
    // CORS will be handled by Spring Cloud Gateway's built-in mechanism
    // configured in application.yml
}
