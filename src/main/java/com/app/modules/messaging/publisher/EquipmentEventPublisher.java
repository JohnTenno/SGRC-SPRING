package com.app.modules.messaging.publisher;

import com.app.modules.messaging.config.RabbitMQConfig;
import com.app.modules.messaging.events.EquipmentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EquipmentEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public void publishCreated(Integer requestId, Integer userId) {
        send(RabbitMQConfig.ROUTING_EQUIPMENT_CREATED,
                new EquipmentEvent(requestId, userId, "PENDING_PICKUP", "CREATED"));
    }

    public void publishUpdated(Integer requestId, Integer userId, String newStatus) {
        send(RabbitMQConfig.ROUTING_EQUIPMENT_UPDATED,
                new EquipmentEvent(requestId, userId, newStatus, "UPDATED"));
    }

    private void send(String routingKey, EquipmentEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EQUIPMENT_EXCHANGE, routingKey, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish equipment event: " + event.getEventType(), e);
        }
    }
}
