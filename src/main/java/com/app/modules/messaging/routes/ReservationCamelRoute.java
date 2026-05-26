package com.app.modules.messaging.routes;

import com.app.modules.messaging.events.ReservationEvent;
import com.app.modules.notification.NotificationService;
import com.app.modules.websocket.WebSocketNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReservationCamelRoute extends RouteBuilder {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WebSocketNotificationService wsService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void configure() {

        onException(Exception.class)
                .log("[Camel] Error procesando evento de reservación: ${exception.message}")
                .handled(true);

        from("direct:reservation.created")
                .routeId("route-reservation-created")
                .log("[Camel] CREATED → ${body}")
                .process(exchange -> {
                    ReservationEvent event = objectMapper.readValue(
                            exchange.getIn().getBody(String.class), ReservationEvent.class);

                    String msg = "Tu reservación #" + event.getReservationId() + " fue registrada. ¡Te esperamos!";
                    notificationService.saveReservationNotification(
                            event.getUserId(), event.getReservationId(), msg);
                    wsService.notifyUser(event.getUserId(), "RESERVATION_CREATED", msg);
                });

        from("direct:reservation.updated")
                .routeId("route-reservation-updated")
                .log("[Camel] UPDATED → ${body}")
                .process(exchange -> {
                    ReservationEvent event = objectMapper.readValue(
                            exchange.getIn().getBody(String.class), ReservationEvent.class);

                    String msg = reservationStatusMessage(event.getReservationId(), event.getStatus());
                    notificationService.saveReservationNotification(
                            event.getUserId(), event.getReservationId(), msg);
                    wsService.notifyUser(event.getUserId(), "RESERVATION_UPDATED", msg);
                });
    }

    private static String reservationStatusMessage(Integer reservationId, String status) {
        String label = switch (status == null ? "" : status) {
            case "APPROVED"   -> "Tu reservación #" + reservationId + " fue aprobada. ¡Te esperamos el día acordado!";
            case "ACTIVE"     -> "Tu reservación #" + reservationId + " está en curso. ¡Disfruta tu espacio!";
            case "COMPLETED"  -> "Tu reservación #" + reservationId + " ha concluido. ¡Gracias por tu visita!";
            case "CANCELLED"  -> "Tu reservación #" + reservationId + " fue cancelada.";
            case "NO_SHOW"    -> "No se registró tu asistencia a la reservación #" + reservationId + ".";
            default           -> "Tu reservación #" + reservationId + " fue actualizada.";
        };
        return label;
    }
}
