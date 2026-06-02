package com.base.config;

import com.base.entity.Role;
import com.base.entity.User;
import com.base.repository.RoleRepository;
import com.base.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseThrow();
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(adminRole)
                    .build();

            Role staffRole = roleRepository.findByRoleName("STAFF")
                    .orElseThrow();
            User staff = User.builder()
                    .username("staff")
                    .email("staff@example.com")
                    .password(passwordEncoder.encode("staff123"))
                    .role(staffRole)
                    .build();

            Role userRole = roleRepository.findByRoleName("USER")
                    .orElseThrow();
            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(userRole)
                    .build();

            userRepository.save(admin);
            userRepository.save(staff);
            userRepository.save(user);
            log.info("Seeded default users: admin / staff / user");
        }
    }
}
