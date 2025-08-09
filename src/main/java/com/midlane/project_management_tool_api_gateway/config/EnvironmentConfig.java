package com.midlane.project_management_tool_api_gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Configuration
public class EnvironmentConfig {

    private final ConfigurableEnvironment environment;

    public EnvironmentConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadEnvironmentVariables() {
        try {
            // Look for .env file in the project root
            Path envPath = Paths.get(".env");
            if (!Files.exists(envPath)) {
                // Try current working directory
                envPath = Paths.get(System.getProperty("user.dir"), ".env");
            }
            
            if (Files.exists(envPath)) {
                Properties envProps = new Properties();
                try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
                    envProps.load(fis);
                    
                    // Add loaded properties to Spring environment with lower precedence
                    environment.getPropertySources().addLast(
                        new PropertiesPropertySource("envFile", envProps)
                    );
                    
                    System.out.println("Successfully loaded environment variables from: " + envPath.toAbsolutePath());
                }
            } else {
                System.out.println("No .env file found. Using system environment variables and defaults.");
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
    }
}
