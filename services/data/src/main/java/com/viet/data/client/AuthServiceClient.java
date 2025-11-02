package com.viet.data.client;

import com.viet.data.dto.response.TokenValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @PostMapping("/auth/validate")
    TokenValidationResponse validateToken(@RequestHeader("Authorization") String token);
}
