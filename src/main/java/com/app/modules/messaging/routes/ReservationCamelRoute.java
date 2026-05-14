package com.app.modules.messaging.routes;

import com.app.modules.messaging.config.RabbitMQConfig;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ReservationCamelRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("spring-rabbitmq:" + RabbitMQConfig.EXCHANGE
                + "?queues=" + RabbitMQConfig.QUEUE_CREATED)
                .routeId("route-reservation-created")
                .log("[Camel] CREATED event → ${body}")
                .to("log:com.app.messaging?level=INFO&showBody=true");

        from("spring-rabbitmq:" + RabbitMQConfig.EXCHANGE
                + "?queues=" + RabbitMQConfig.QUEUE_UPDATED)
                .routeId("route-reservation-updated")
                .log("[Camel] UPDATED event → ${body}")
                .to("log:com.app.messaging?level=INFO&showBody=true");
    }
}
