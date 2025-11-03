package com.viet.auth.config;

import com.viet.auth.module.Role;
import com.viet.auth.module.User;
import com.viet.auth.repository.RoleRepository;
import com.viet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:vanviet0611@gmail.com}")
    private String adminEmail;

    @Value("${app.admin.password:viet123}")
    private String adminPassword;

    @Value("${app.admin.fullname:LeVanViet}")
    private String adminFullName;

    @Override
    public void run(String... args) throws Exception {
        createRoles();
        createAdminUser();
    }

    private void createRoles() {
        if (roleRepository.count() == 0) {
            Role guestRole = new Role(Role.RoleName.ROLE_GUEST, "Guest user with limited access");
            Role userRole = new Role(Role.RoleName.ROLE_USER, "Regular authenticated user");
            Role adminRole = new Role(Role.RoleName.ROLE_ADMIN, "Administrator with full access");
            Role superAdminRole = new Role(Role.RoleName.ROLE_SUPER_ADMIN, "Super administrator");

            roleRepository.saveAll(List.of(guestRole, userRole, adminRole, superAdminRole));
            log.info("Default roles created successfully");
        } else {
            log.info("Roles already exist in database");
        }
    }

    private void createAdminUser() {
        // Sử dụng method findByEmail đúng với User entity
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            // Lấy tất cả roles (ADMIN và SUPER_ADMIN)
            Set<Role> adminRoles = roleRepository.findAll().stream()
                    .filter(role -> role.getName() == Role.RoleName.ROLE_ADMIN ||
                            role.getName() == Role.RoleName.ROLE_SUPER_ADMIN)
                    .collect(Collectors.toSet());

            // Tạo admin user với fields đúng theo User entity
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setFullName(adminFullName); //
            adminUser.setRoles(adminRoles);
            adminUser.setIsActive(true);
            adminUser.setIsVerified(true); // Admin nên được verified

            userRepository.save(adminUser);
            log.info("Admin user created successfully");
            log.info("Email: {}", adminEmail);
            log.info("Password: {}", adminPassword);
            log.info("Full Name: {}", adminFullName);
            log.info("Roles: {}", adminRoles.stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.joining(", ")));

        } else {
            log.info("Admin user already exists");
        }
    }
}