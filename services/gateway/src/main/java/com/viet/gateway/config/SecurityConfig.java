package com.viet.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(
                                "/api/auth/register",  // ✅ THIẾU DẤU / Ở ĐẦU
                                "/api/auth/login",
                                "/api/auth/guest",
                                "/api/auth/refresh",   // ✅ THIẾU DẤU / Ở ĐẦU
                                "/api/data/health",
                                "/actuator/health",
                                "/actuator/info",
                                "/eureka/**",
                                "/fallback/**",
                                "/health"
                        ).permitAll()
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}