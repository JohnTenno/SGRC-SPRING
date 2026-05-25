package com.app.modules.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "faculty_id", nullable = false)
    private Integer facultyId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, unique = true, length = 20)
    private String enrollment;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "is_tutor", nullable = false)
    private boolean tutor;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "logo_url", nullable = false, length = 255)
    private String logoUrl;

    public User() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Integer facultyId) {
        this.facultyId = facultyId;
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

    public String getEnrollment() {
        return enrollment;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isTutor() {
        return tutor;
    }

    public void setTutor(boolean tutor) {
        this.tutor = tutor;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
