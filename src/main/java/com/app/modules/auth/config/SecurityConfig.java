package com.app.modules.auth.config;

import com.app.modules.auth.filter.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
                this.jwtAuthFilter = jwtAuthFilter;
        }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> {
                                })
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api", "/api/auth/**").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .requestMatchers("/ws/**").permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/cubicles",
                                                                "/api/cubicles/**",
                                                                "/api/equipment-types",
                                                                "/api/equipment-types/**",
                                                                "/api/tutoring/subjects",
                                                                "/api/tutoring/subjects/**",
                                                                "/api/tutoring/professors",
                                                                "/api/tutoring/professors/**")
                                                .permitAll()
                                                .requestMatchers("/api/notifications/**").authenticated()
                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((req, res, e) -> res.sendError(
                                                                HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }
}
