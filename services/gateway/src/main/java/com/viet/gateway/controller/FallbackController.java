package com.viet.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "errorCode", "AUTH_SERVICE_UNAVAILABLE",
                        "message", "Authentication service is temporarily unavailable",
                        "timestamp", System.currentTimeMillis()
                )));
    }

    @RequestMapping("/fallback/data")
    public Mono<ResponseEntity<Map<String, Object>>> dataServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "errorCode", "DATA_SERVICE_UNAVAILABLE",
                        "message", "Data processing service is temporarily unavailable",
                        "timestamp", System.currentTimeMillis()
                )));
    }

    @RequestMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "timestamp", System.currentTimeMillis()
        )));
    }
}
