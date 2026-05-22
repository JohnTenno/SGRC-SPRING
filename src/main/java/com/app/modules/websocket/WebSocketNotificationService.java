package com.app.modules.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyCubicleCheckIn(Integer cubicleId, String studentName, Integer reservationId, String endTime) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "CHECKED_IN");
        payload.put("studentName", studentName);
        payload.put("reservationId", reservationId);
        payload.put("endTime", endTime);
        messagingTemplate.convertAndSend("/topic/cubicle/" + cubicleId, (Object) payload);
    }

    public void notifyCubicleCheckout(Integer cubicleId, Integer reservationId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "CHECKED_OUT");
        payload.put("reservationId", reservationId);
        messagingTemplate.convertAndSend("/topic/cubicle/" + cubicleId, (Object) payload);
    }

    public void notifyCubicleNoShow(Integer cubicleId, Integer reservationId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "NO_SHOW");
        payload.put("reservationId", reservationId);
        messagingTemplate.convertAndSend("/topic/cubicle/" + cubicleId, (Object) payload);
    }

    public void notifyUser(Integer userId, String event, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", event);
        payload.put("message", message);
        messagingTemplate.convertAndSend("/topic/user/" + userId, (Object) payload);
    }

    public void notifyAdminReservationUpdate(Integer reservationId, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", reservationId);
        payload.put("status", status);
        messagingTemplate.convertAndSend("/topic/admin/reservations", (Object) payload);
    }
}
