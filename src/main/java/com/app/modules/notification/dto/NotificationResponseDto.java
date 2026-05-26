package com.app.modules.notification.dto;

import com.app.modules.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationResponseDto {

    private Integer id;
    private Integer reservationId;
    private String message;
    @JsonProperty("isRead")
    private boolean isRead;
    private String expirationDate;

    public NotificationResponseDto(Notification n) {
        this.id = n.getId();
        this.reservationId = n.getReservationId();
        this.message = n.getMessage();
        this.isRead = n.isRead();
        this.expirationDate = n.getExpirationDate().toString();
    }

    public Integer getId() { return id; }
    public Integer getReservationId() { return reservationId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getExpirationDate() { return expirationDate; }
}
