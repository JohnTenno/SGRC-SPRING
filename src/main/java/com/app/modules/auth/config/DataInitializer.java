package com.app.modules.auth.config;

import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String LEGACY_INVALID_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72";
    private static final String DEFAULT_PASSWORD = "password123";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findAll().forEach(this::ensureValidPassword);
    }

    private void ensureValidPassword(User user) {
        String hash = user.getPassword();
        boolean missing = hash == null || hash.isBlank();
        boolean legacy = LEGACY_INVALID_HASH.equals(hash);

        if (missing || legacy) {
            user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            userRepository.save(user);
        }
    }
}
