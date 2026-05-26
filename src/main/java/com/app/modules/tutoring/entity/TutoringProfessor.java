package com.app.modules.tutoring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tutoring_professor")
public class TutoringProfessor {

    @Id
    @Column(name = "employee_number", nullable = false, length = 20)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    public TutoringProfessor() {
    }

    @Transient
    public String getFullName() {
        if (firstName == null && lastName == null)
            return null;
        if (lastName == null || lastName.isBlank())
            return firstName;
        if (firstName == null || firstName.isBlank())
            return lastName;
        return firstName + " " + lastName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            firstName = "";
            lastName = "";
            return;
        }
        int split = fullName.trim().lastIndexOf(' ');
        if (split < 0) {
            firstName = fullName.trim();
            lastName = "";
        } else {
            firstName = fullName.substring(0, split).trim();
            lastName = fullName.substring(split + 1).trim();
        }
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
