package com.app.modules.messaging.events;

import java.time.LocalDateTime;

public class ReservationEvent {

    private Integer reservationId;
    private String status;
    private String eventType;
    private LocalDateTime timestamp;

    public ReservationEvent() {
    }

    public ReservationEvent(Integer reservationId, String status, String eventType) {
        this.reservationId = reservationId;
        this.status = status;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ReservationEvent{id=" + reservationId +
                ", type=" + eventType +
                ", status=" + status +
                ", at=" + timestamp + "}";
    }
}
