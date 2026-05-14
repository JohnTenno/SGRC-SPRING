package com.app.modules.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateReservationDto {
    private Integer userId;
    private Integer cubicleId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCubicleId() {
        return cubicleId;
    }

    public void setCubicleId(Integer cubicleId) {
        this.cubicleId = cubicleId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
