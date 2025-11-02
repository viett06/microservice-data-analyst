package com.viet.data.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityUtils {

    // ✅ XÓA TỪ KHÓA STATIC để có thể inject dependency
    public String getCurrentUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id header is missing");
            throw new SecurityException("User ID not found in request");
        }
        return userId;
    }

    public String getCurrentUserRole(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role == null || role.isEmpty()) {
            log.warn("X-User-Role header is missing");
            return "USER"; // Default role
        }
        return role;
    }

    public String getCurrentUserEmail(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        if (email == null || email.isEmpty()) {
            log.warn("X-User-Email header is missing");
            return "unknown@example.com";
        }
        return email;
    }

    public boolean isAdmin(HttpServletRequest request) {
        String roles = request.getHeader("X-User-Role");
        return roles != null && roles.contains("ADMIN");
    }

    public void validateUserAccess(HttpServletRequest request, String resourceUserId) {
        String currentUserId = getCurrentUserId(request);

        // Admin có quyền truy cập mọi thứ
        if (isAdmin(request)) {
            return;
        }

        // User chỉ có quyền truy cập tài nguyên của chính họ
        if (!currentUserId.equals(resourceUserId)) {
            log.warn("User {} attempted to access resource of user {}", currentUserId, resourceUserId);
            throw new SecurityException("Access denied: User cannot access this resource");
        }
    }
}