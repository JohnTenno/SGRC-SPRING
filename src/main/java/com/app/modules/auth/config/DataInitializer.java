package com.app.modules.auth.config;

import com.app.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findAll().stream()
                .filter(user -> user.getPassword() == null || user.getPassword().isBlank())
                .forEach(user -> {
                    user.setPassword(passwordEncoder.encode("password"));
                    userRepository.save(user);
                });
    }
}
