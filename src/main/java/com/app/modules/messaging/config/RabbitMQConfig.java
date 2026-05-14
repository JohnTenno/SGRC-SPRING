package com.app.modules.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "reservation.exchange";
    public static final String QUEUE_CREATED = "reservation.created";
    public static final String QUEUE_UPDATED = "reservation.updated";
    public static final String ROUTING_CREATED = "reservation.created";
    public static final String ROUTING_UPDATED = "reservation.updated";

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
}
