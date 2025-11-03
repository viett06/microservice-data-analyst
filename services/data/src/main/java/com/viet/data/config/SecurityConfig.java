package com.viet.data.config;

import com.viet.data.security.InternalAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final InternalAuthFilter internalAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.disable()) // TẮT CORS (gateway đã xử lý)
                .csrf(csrf -> csrf.disable()) // TẮT CSRF (API stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints (Gateway cần access)
                        .requestMatchers("/api/data/health").permitAll() // Health check
                        .requestMatchers("/actuator/health").permitAll() // Actuator
                        .requestMatchers("/api/data/**").authenticated()

                        .anyRequest().authenticated() // Tất cả endpoints khác cần auth
                )
                .addFilterBefore(internalAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable()) // TẮT Basic Auth
                .formLogin(formLogin -> formLogin.disable()) // TẮT Form Login
                .build();
    }
}