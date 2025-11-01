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
public class TokenValidationResponse {
    private Boolean valid;
    private String username;
    private Long userId;
    private Set<Role.RoleName> roles;
    private String message;
}