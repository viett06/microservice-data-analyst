package com.viet.auth.config;

import com.viet.auth.module.Role;
import com.viet.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        createRoles();
    }

    private void createRoles() {
        if (roleRepository.count() == 0) {
            Role guestRole = new Role(Role.RoleName.ROLE_GUEST, "Guest user with limited access");
            Role userRole = new Role(Role.RoleName.ROLE_USER, "Regular authenticated user");
            Role adminRole = new Role(Role.RoleName.ROLE_ADMIN, "Administrator with full access");
            Role superAdminRole = new Role(Role.RoleName.ROLE_SUPER_ADMIN, "Super administrator");

            roleRepository.save(guestRole);
            roleRepository.save(userRole);
            roleRepository.save(adminRole);
            roleRepository.save(superAdminRole);

            log.info("Default roles created successfully");
        }
    }
}
