package com.midlane.project_management_tool_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@GetMapping("/health")
	public Mono<String> health() {
		return Mono.just("API Gateway is running!");
	}

	@GetMapping("/")
	public Mono<String> home() {
		return Mono.just("Welcome to Project Management Tool API Gateway");
	}
}
