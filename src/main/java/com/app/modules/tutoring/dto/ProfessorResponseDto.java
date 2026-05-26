package com.app.modules.tutoring.dto;

import com.app.modules.tutoring.entity.TutoringProfessor;

public class ProfessorResponseDto {
    private String employeeNumber;
    private String fullName;
    private String bio;

    public ProfessorResponseDto(TutoringProfessor p) {
        this.employeeNumber = p.getEmployeeNumber();
        this.fullName = p.getFullName();
        this.bio = p.getBio();
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBio() {
        return bio;
    }
}
