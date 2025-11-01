package com.viet.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthStatsResponse {
    private Long totalUsers;
    private Long activeUsers;
}