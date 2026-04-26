package com.shopflow;

import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.repository.RoleRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ROLES
        createRoleIfNotExists(Role.RoleName.ROLE_ADMIN);
        createRoleIfNotExists(Role.RoleName.ROLE_SELLER);
        createRoleIfNotExists(Role.RoleName.ROLE_CUSTOMER);
        System.out.println(" Roles initialized successfully");

        // ADMIN
        if (!userRepository.existsByEmail("admin@shopflow.com")) {
            Role adminRole = roleRepository
                    .findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow();

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("ShopFlow")
                    .email("admin@shopflow.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin created: admin@shopflow.com / admin123");
        }
    }

    private void createRoleIfNotExists(Role.RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}