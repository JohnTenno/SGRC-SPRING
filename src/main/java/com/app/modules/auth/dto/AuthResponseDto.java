package com.app.modules.auth.dto;

public class AuthResponseDto {
    private String token;
    private String enrollment;
    private String role;

    public AuthResponseDto(String token, String enrollment, String role) {
        this.token = token;
        this.enrollment = enrollment;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getEnrollment() {
        return enrollment;
    }

    public String getRole() {
        return role;
    }
}
