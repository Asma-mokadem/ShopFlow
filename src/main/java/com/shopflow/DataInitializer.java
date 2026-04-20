package com.shopflow;

import com.shopflow.entity.Role;
import com.shopflow.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotExists(Role.RoleName.ROLE_ADMIN);
        createRoleIfNotExists(Role.RoleName.ROLE_SELLER);
        createRoleIfNotExists(Role.RoleName.ROLE_CUSTOMER);
        System.out.println("✅ Roles initialized successfully");
    }

    private void createRoleIfNotExists(Role.RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}