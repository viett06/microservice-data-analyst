package com.viet.auth.dto.response;

import com.viet.auth.module.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private Set<Role.RoleName> roles;
    private Long expiresIn;
    private Boolean isGuest = false;

    @Builder.Default
    private String message = "Authentication successful";
}
