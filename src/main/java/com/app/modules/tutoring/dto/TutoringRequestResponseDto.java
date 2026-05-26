package com.app.modules.tutoring.dto;

import com.app.modules.tutoring.entity.TutoringRequest;

public class TutoringRequestResponseDto {
    private Integer id;
    private String studentEnrollment;
    private String professorEmployeeNumber;
    private String subject;
    private String reservationDate;
    private String startTime;
    private String endTime;
    private String topic;
    private String status;
    private String createdAt;

    public TutoringRequestResponseDto(TutoringRequest r) {
        this.id = r.getId();
        this.studentEnrollment = r.getStudentEnrollment();
        this.professorEmployeeNumber = r.getProfessorEmployeeNumber();
        this.subject = r.getSubject();
        this.reservationDate = r.getReservationDate() != null ? r.getReservationDate().toString() : null;
        this.startTime = r.getStartTime();
        this.endTime = r.getEndTime();
        this.topic = r.getTopic();
        this.status = r.getStatus();
        this.createdAt = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
    }

    public Integer getId() {
        return id;
    }

    public String getStudentEnrollment() {
        return studentEnrollment;
    }

    public String getProfessorEmployeeNumber() {
        return professorEmployeeNumber;
    }

    public String getSubject() {
        return subject;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getTopic() {
        return topic;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
