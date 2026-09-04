package com.ayesha.resolvehub.config;

import com.ayesha.resolvehub.entity.Role;
import com.ayesha.resolvehub.entity.User;
import com.ayesha.resolvehub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test & !prod")
public class SecurityDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUserIfMissing("admin@resolvehub.com", "Admin User", Role.ADMIN, "admin123");
        seedUserIfMissing("agent@resolvehub.com", "Support Agent", Role.AGENT, "agent123");
        seedUserIfMissing("reporter@resolvehub.com", "Reporter User", Role.REPORTER, "reporter123");
    }

    private void seedUserIfMissing(String email, String name, Role role, String rawPassword) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User(
                null,
                name,
                email,
                role,
                passwordEncoder.encode(rawPassword)
            );
            userRepository.save(user);
            log.info("Seeded default development user: {} (Role: {})", email, role);
        }
    }
}
