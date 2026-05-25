package com.app.modules.auth.dto;

public class AuthResponseDto {

    private String token;
    private UserInfo user;

    public AuthResponseDto(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserInfo getUser() {
        return user;
    }

    public static class UserInfo {
        private Integer id;
        private String enrollment;
        private String name;
        private String email;
        private String role;
        private String logoUrl;
        private boolean isTutor;
        private Integer facultyId;

        public UserInfo(Integer id, String enrollment, String name, String email,
                String role, String logoUrl, boolean isTutor, Integer facultyId) {
            this.id = id;
            this.enrollment = enrollment;
            this.name = name;
            this.email = email;
            this.role = role;
            this.isTutor = isTutor;
            this.facultyId = facultyId;
            this.logoUrl = logoUrl;
        }

        public Integer getId() {
            return id;
        }

        public String getEnrollment() {
            return enrollment;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public boolean getIsTutor() {
            return isTutor;
        }

        public Integer getFacultyId() {
            return facultyId;
        }
    }
}
