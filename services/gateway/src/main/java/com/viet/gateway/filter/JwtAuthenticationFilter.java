package com.viet.gateway.filter;

import com.viet.gateway.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final AuthService authService;

    public JwtAuthenticationFilter(AuthService authService) {
        super(Config.class);
        this.authService = authService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip authentication for public endpoints
            if (isPublicEndpoint(request.getPath().toString())) {
                return chain.filter(exchange);
            }

            // Get JWT token from header
            String token = getJwtFromRequest(request);

            if (!StringUtils.hasText(token)) {
                log.warn("No JWT token found in request to: {}", request.getPath());
                return unauthorizedResponse(exchange, "Missing authentication token");
            }

            // Validate token with Auth Service
            return authService.validateToken(token)
                    .flatMap(validationResponse -> {
                        if (validationResponse.getValid()) {
                            // Add user info to headers for downstream services
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", validationResponse.getUserId().toString())
                                    .header("X-User-Role", String.join(",", validationResponse.getRoles()))
                                    .header("X-User-Email", validationResponse.getUsername())
                                    .header("X-Internal-Auth", "true")
                                    .build();

                            log.debug("User authenticated: {} with roles: {}",
                                    validationResponse.getUsername(), validationResponse.getRoles());

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        } else {
                            log.warn("Invalid JWT token for request to: {}", request.getPath());
                            return unauthorizedResponse(exchange, "Invalid or expired token");
                        }
                    })
                    .onErrorResume(throwable -> {
                        log.error("Error validating token: {}", throwable.getMessage());
                        return unauthorizedResponse(exchange, "Token validation failed");
                    });
        };
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/guest") ||
                path.startsWith("/api/auth/refresh") ||
                path.equals("/api/data/health") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/eureka/");
    }

    private String getJwtFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}
