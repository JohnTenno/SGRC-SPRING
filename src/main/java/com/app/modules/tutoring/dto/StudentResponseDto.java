package com.app.modules.tutoring.dto;

import com.app.modules.user.entity.User;

public class StudentResponseDto {
    private String enrollment;
    private String fullName;
    private String email;
    private boolean isTutor;

    public StudentResponseDto(User u) {
        this.enrollment = u.getEnrollment();
        this.fullName = u.getFullName();
        this.email = u.getEmail();
        this.isTutor = u.isTutor();
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

    public boolean isTutor() {
        return isTutor;
    }
}
