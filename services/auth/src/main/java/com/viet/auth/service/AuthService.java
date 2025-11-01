package com.viet.auth.service;

import com.viet.auth.dto.request.LoginRequest;
import com.viet.auth.dto.request.RefreshTokenRequest;
import com.viet.auth.dto.request.RegisterRequest;
import com.viet.auth.dto.response.AuthResponse;
import com.viet.auth.dto.response.TokenValidationResponse;
import com.viet.auth.exception.InvalidTokenException;
import com.viet.auth.exception.UserAlreadyExistsException;
import com.viet.auth.exception.UserNotFoundException;
import com.viet.auth.module.GuestSession;
import com.viet.auth.module.RefreshToken;
import com.viet.auth.module.Role;
import com.viet.auth.module.User;
import com.viet.auth.repository.GuestSessionRepository;
import com.viet.auth.repository.RefreshTokenRepository;
import com.viet.auth.repository.RoleRepository;
import com.viet.auth.repository.UserRepository;
import com.viet.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GuestSessionRepository guestSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setIsActive(true);
        user.setIsVerified(false);

        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(savedUser);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser);

        // Save refresh token
        saveRefreshToken(savedUser, refreshToken);

        return buildAuthResponse(savedUser, accessToken, refreshToken, false);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmailAndActive(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getEmail()));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        log.info("User logged in successfully: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken, false);
    }

    @Transactional
    public AuthResponse createGuestSession() {
        GuestSession guestSession = new GuestSession();
        guestSession.setSessionId(UUID.randomUUID().toString());
        guestSession.setExpiresAt(LocalDateTime.now().plusHours(24));
        guestSession.setIsActive(true);
        guestSession.setLastActivity(LocalDateTime.now());

        GuestSession savedSession = guestSessionRepository.save(guestSession);

        String accessToken = tokenProvider.generateGuestToken(savedSession);

        log.info("Guest session created: {}", savedSession.getSessionId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(savedSession.getId())
                .email("guest@" + savedSession.getSessionId())
                .fullName("Guest User")
                .roles(Set.of(Role.RoleName.ROLE_GUEST))
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .isGuest(true)
                .message("Guest session created successfully")
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        if (!tokenProvider.validateToken(requestRefreshToken)) {
            throw new InvalidTokenException("Refresh token is invalid");
        }

        String tokenType = tokenProvider.getTokenType(requestRefreshToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(requestRefreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Verify refresh token exists in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found in database"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token was expired");
        }

        // Generate new tokens
        String newAccessToken = tokenProvider.generateAccessToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        // Update refresh token in database
        refreshToken.setToken(newRefreshToken);
        refreshToken.setExpiryDate(
                LocalDateTime.ofInstant(
                        Instant.now().plusMillis(tokenProvider.getAccessTokenExpiration()),
                        ZoneId.systemDefault()
                )
        );

        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed for user: {}", user.getEmail());
        return buildAuthResponse(user, newAccessToken, newRefreshToken, false);
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            if (!tokenProvider.validateToken(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token is invalid")
                        .build();
            }

            String username = tokenProvider.getUsernameFromToken(token);
            Long userId = tokenProvider.getUserIdFromToken(token);
            Set<Role.RoleName> roles = tokenProvider.getRolesFromToken(token);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .username(username)
                    .userId(userId)
                    .roles(roles)
                    .message("Token is valid")
                    .build();

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }

        // Clear security context
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteAllExpiredSince(now);
        guestSessionRepository.deleteAllExpiredSince(now);
        log.info("Cleaned up expired tokens and sessions");
    }

    private void saveRefreshToken(User user, String refreshToken) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(refreshToken);
        rt.setExpiryDate(LocalDateTime.ofInstant(
                Instant.now().plusMillis(tokenProvider.getAccessTokenExpiration()),
                ZoneId.systemDefault()
        ));
        refreshTokenRepository.save(rt);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken, boolean isGuest) {
        Set<Role.RoleName> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .isGuest(isGuest)
                .message("Authentication successful")
                .build();
    }
}
