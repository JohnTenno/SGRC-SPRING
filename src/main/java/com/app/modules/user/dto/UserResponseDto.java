package com.app.modules.user.dto;

import com.app.modules.user.entity.User;

public class UserResponseDto {
    private Integer id;
    private Integer facultyId;
    private String enrollment;
    private String fullName;
    private String email;
    private String role;
    private boolean blocked;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.facultyId = user.getFacultyId();
        this.enrollment = user.getEnrollment();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.blocked = user.isBlocked();
    }

    public Integer getId() {
        return id;
    }

    public Integer getFacultyId() {
        return facultyId;
    }

    public String getEnrollment() {
        return enrollment;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
