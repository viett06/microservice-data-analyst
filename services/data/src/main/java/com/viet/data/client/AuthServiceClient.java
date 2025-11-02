package com.viet.data.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${app.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    @PostMapping("/auth/validate")
    Object validateToken(@RequestHeader("Authorization") String token);

    // Additional auth service methods can be added here
}
