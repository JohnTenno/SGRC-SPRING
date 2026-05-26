package com.app.modules.messaging.routes;

import com.app.modules.messaging.events.EquipmentEvent;
import com.app.modules.notification.NotificationService;
import com.app.modules.websocket.WebSocketNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EquipmentCamelRoute extends RouteBuilder {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WebSocketNotificationService wsService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void configure() {

        onException(Exception.class)
                .log("[Camel] Error procesando evento de equipo: ${exception.message}")
                .handled(true);

        from("direct:equipment.created")
                .routeId("route-equipment-created")
                .log("[Camel] EQUIPMENT CREATED → ${body}")
                .process(exchange -> {
                    EquipmentEvent event = objectMapper.readValue(
                            exchange.getIn().getBody(String.class), EquipmentEvent.class);

                    wsService.notifyAdminEquipmentRequest(
                            event.getRequestId(), event.getUserId(), event.getStatus());
                });

        from("direct:equipment.updated")
                .routeId("route-equipment-updated")
                .log("[Camel] EQUIPMENT UPDATED → ${body}")
                .process(exchange -> {
                    EquipmentEvent event = objectMapper.readValue(
                            exchange.getIn().getBody(String.class), EquipmentEvent.class);

                    String msg = equipmentStatusMessage(event.getRequestId(), event.getStatus());
                    notificationService.saveEquipmentNotification(event.getUserId(), msg);
                    wsService.notifyUser(event.getUserId(), "EQUIPMENT_UPDATED", msg);
                });
    }

    private static String equipmentStatusMessage(Integer requestId, String status) {
        return switch (status == null ? "" : status) {
            case "READY_FOR_PICKUP" -> "Tu solicitud #" + requestId + " está lista para recoger. Pasa a recoger tu equipo.";
            case "AWAITING_RETURN"  -> "Tu préstamo #" + requestId + " está activo. Recuerda devolverlo a tiempo.";
            case "COMPLETED"        -> "Tu préstamo #" + requestId + " fue cerrado correctamente. ¡Gracias!";
            case "CANCELLED"        -> "Tu solicitud de equipo #" + requestId + " fue cancelada.";
            default                 -> "Tu solicitud de equipo #" + requestId + " fue actualizada.";
        };
    }
}
