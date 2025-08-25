package com.midlane.project_management_tool_api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class GatewayConfig {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;
}
