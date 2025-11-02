package com.viet.gateway.service;

import com.viet.gateway.model.TokenValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient.Builder webClientBuilder;

    public Mono<TokenValidationResponse> validateToken(String token) {
        return webClientBuilder.build()
                .post()
                .uri("http://auth-service/auth/validate")  // ✅ SỬA URL
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .doOnSuccess(response -> log.debug("Token validation result: {}", response.getValid())) // ✅ SỬA getValid()
                .doOnError(error -> log.error("Error calling auth service: {}", error.getMessage()))
                .onErrorReturn(TokenValidationResponse.invalid("Auth service unavailable"));
    }
}