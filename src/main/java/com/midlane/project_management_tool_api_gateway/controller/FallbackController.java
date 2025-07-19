package com.midlane.project_management_tool_api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth-service")
    public Mono<ResponseEntity<Map<String, Object>>> authServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Auth service is currently unavailable");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", "Please try again later");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "User service is currently unavailable");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", "Please try again later");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/project-service")
    public Mono<ResponseEntity<Map<String, Object>>> projectServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Project service is currently unavailable");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", "Please try again later");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/task-service")
    public Mono<ResponseEntity<Map<String, Object>>> taskServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Task service is currently unavailable");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", "Please try again later");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
