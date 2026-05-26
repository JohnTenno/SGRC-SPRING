package com.app.modules.messaging.events;

import java.time.LocalDateTime;

public class EquipmentEvent {

    private Integer requestId;
    private Integer userId;
    private String status;
    private String eventType;
    private LocalDateTime timestamp;

    public EquipmentEvent() {
    }

    public EquipmentEvent(Integer requestId, Integer userId, String status, String eventType) {
        this.requestId = requestId;
        this.userId = userId;
        this.status = status;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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
        return "EquipmentEvent{requestId=" + requestId +
                ", userId=" + userId +
                ", type=" + eventType +
                ", status=" + status +
                ", at=" + timestamp + "}";
    }
}
