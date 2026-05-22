package com.app.modules.auth;

import com.app.modules.auth.dto.AuthResponseDto;
import com.app.modules.auth.dto.LoginDto;
import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String enrollment) throws UsernameNotFoundException {
        User user = userRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + enrollment));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEnrollment())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    public AuthResponseDto login(LoginDto dto) {
        User user = userRepository.findByEnrollment(dto.getEnrollment())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEnrollment());
        AuthResponseDto.UserInfo userInfo = new AuthResponseDto.UserInfo(
                user.getId(),
                user.getEnrollment(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isTutor(),
                user.getFacultyId());
        return new AuthResponseDto(token, userInfo);
    }
}
