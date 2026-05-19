package com.app.modules.user.entity;

import com.app.modules.faculty.entity.Faculty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

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

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String password;

    public User() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    @Transient
    public String getFullName() {
        if (firstName == null && lastName == null) return null;
        if (lastName == null || lastName.isBlank()) return firstName;
        if (firstName == null || firstName.isBlank()) return lastName;
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

    public String getEnrollment() { return enrollment; }
    public void setEnrollment(String enrollment) { this.enrollment = enrollment; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
