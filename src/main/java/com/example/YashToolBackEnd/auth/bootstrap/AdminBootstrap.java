package com.example.YashToolBackEnd.auth.bootstrap;


import com.example.YashToolBackEnd.auth.entity.Role;
import com.example.YashToolBackEnd.auth.entity.User;
import com.example.YashToolBackEnd.auth.entity.enums.RoleName;
import com.example.YashToolBackEnd.auth.repository.RoleRepository;
import com.example.YashToolBackEnd.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.email}")
    private String adminEmail;

    @Value("${app.default-admin.password}")
    private String adminPassword;

    @Value("${app.default-admin.phone:0000000000}")
    private String adminPhone;

    @Override
    public void run(String @NonNull ... args) {

        if (userRepository.count() > 0) {
            log.info("Users already exist. Skipping ADMIN bootstrap.");
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));


        User admin = new User();
        admin.setName("System Administrator");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setPhone(adminPhone);
        admin.setEnabled(true);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);

        log.info("===============================================");
        log.info("DEFAULT ADMIN USER CREATED");
        log.info("Email: {}", adminEmail);
        log.info("Please change password after first login");
        log.info("===============================================");
    }
}
