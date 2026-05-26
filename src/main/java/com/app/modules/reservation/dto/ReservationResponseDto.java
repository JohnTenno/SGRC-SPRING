package com.app.modules.reservation.dto;

import com.app.modules.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservationResponseDto {
    private Integer id;
    private Integer userId;
    private String userFullName;
    private Integer cubicleId;
    private String cubicleIdentifier;
    private String cubicleLogoUrl;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private LocalDateTime createdAt;

    public ReservationResponseDto(Reservation reservation) {
        this.id = reservation.getId();
        this.userId = reservation.getUser().getId();
        this.userFullName = reservation.getUser().getFullName();
        this.cubicleId = reservation.getCubicle().getId();
        this.cubicleIdentifier = reservation.getCubicle().getIdentifier();
        this.cubicleLogoUrl = reservation.getCubicle().getLogoUrl();
        this.date = reservation.getDate();
        this.startTime = reservation.getStartTime();
        this.endTime = reservation.getEndTime();
        this.status = reservation.getStatus();
        this.createdAt = reservation.getCreatedAt();
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public Integer getCubicleId() {
        return cubicleId;
    }

    public String getCubicleIdentifier() {
        return cubicleIdentifier;
    }

    public String getCubicleLogoUrl() {
        return cubicleLogoUrl;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
