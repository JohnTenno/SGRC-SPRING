package com.app.modules.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Reservation ───────────────────────────────────────────────────────────────
    public static final String EXCHANGE          = "reservation.exchange";
    public static final String QUEUE_CREATED     = "reservation.created";
    public static final String QUEUE_UPDATED     = "reservation.updated";
    public static final String ROUTING_CREATED   = "reservation.created";
    public static final String ROUTING_UPDATED   = "reservation.updated";

    // ── Equipment ─────────────────────────────────────────────────────────────────
    public static final String EQUIPMENT_EXCHANGE         = "equipment.exchange";
    public static final String QUEUE_EQUIPMENT_CREATED    = "equipment.request.created";
    public static final String QUEUE_EQUIPMENT_UPDATED    = "equipment.request.updated";
    public static final String ROUTING_EQUIPMENT_CREATED  = "equipment.request.created";
    public static final String ROUTING_EQUIPMENT_UPDATED  = "equipment.request.updated";

    // ── Reservation beans ─────────────────────────────────────────────────────────
    @Bean
    TopicExchange reservationExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue reservationCreatedQueue() {
        return new Queue(QUEUE_CREATED, true);
    }

    @Bean
    Queue reservationUpdatedQueue() {
        return new Queue(QUEUE_UPDATED, true);
    }

    @Bean
    Binding bindingCreated(Queue reservationCreatedQueue, TopicExchange reservationExchange) {
        return BindingBuilder.bind(reservationCreatedQueue).to(reservationExchange).with(ROUTING_CREATED);
    }

    @Bean
    Binding bindingUpdated(Queue reservationUpdatedQueue, TopicExchange reservationExchange) {
        return BindingBuilder.bind(reservationUpdatedQueue).to(reservationExchange).with(ROUTING_UPDATED);
    }

    // ── Equipment beans ───────────────────────────────────────────────────────────
    @Bean
    TopicExchange equipmentExchange() {
        return new TopicExchange(EQUIPMENT_EXCHANGE);
    }

    @Bean
    Queue equipmentCreatedQueue() {
        return new Queue(QUEUE_EQUIPMENT_CREATED, true);
    }

    @Bean
    Queue equipmentUpdatedQueue() {
        return new Queue(QUEUE_EQUIPMENT_UPDATED, true);
    }

    @Bean
    Binding bindingEquipmentCreated(Queue equipmentCreatedQueue, TopicExchange equipmentExchange) {
        return BindingBuilder.bind(equipmentCreatedQueue).to(equipmentExchange).with(ROUTING_EQUIPMENT_CREATED);
    }

    @Bean
    Binding bindingEquipmentUpdated(Queue equipmentUpdatedQueue, TopicExchange equipmentExchange) {
        return BindingBuilder.bind(equipmentUpdatedQueue).to(equipmentExchange).with(ROUTING_EQUIPMENT_UPDATED);
    }
}
