package com.app.modules.messaging.publisher;

import com.app.modules.messaging.config.RabbitMQConfig;
import com.app.modules.messaging.events.ReservationEvent;
import com.app.modules.reservation.entity.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public void publishCreated(Reservation reservation) {
        Integer userId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        send(RabbitMQConfig.ROUTING_CREATED, new ReservationEvent(
                reservation.getId(), userId, reservation.getStatus(), "CREATED"));
    }

    public void publishUpdated(Reservation reservation) {
        Integer userId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        send(RabbitMQConfig.ROUTING_UPDATED, new ReservationEvent(
                reservation.getId(), userId, reservation.getStatus(), "UPDATED"));
    }

    private void send(String routingKey, ReservationEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish reservation event: " + event.getEventType(), e);
        }
    }
}
