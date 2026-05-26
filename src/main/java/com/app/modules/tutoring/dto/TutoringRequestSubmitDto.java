package com.app.modules.tutoring.dto;

import java.time.LocalDate;

public class TutoringRequestSubmitDto {
    private String professorEmployeeNumber;
    private String subject;
    private LocalDate reservationDate;
    private String startTime;
    private String endTime;
    private String topic;

    public String getProfessorEmployeeNumber() {
        return professorEmployeeNumber;
    }

    public void setProfessorEmployeeNumber(String professorEmployeeNumber) {
        this.professorEmployeeNumber = professorEmployeeNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
