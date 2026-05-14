package com.app.modules.user.dto;

import com.app.modules.user.entity.User;

public class UserResponseDto {
    private Integer id;
    private Integer facultyId;
    private String facultyName;
    private String enrollment;
    private String fullName;
    private String email;
    private String role;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.facultyId = user.getFaculty().getId();
        this.facultyName = user.getFaculty().getName();
        this.enrollment = user.getEnrollment();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public Integer getId() {
        return id;
    }

    public Integer getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
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
}
