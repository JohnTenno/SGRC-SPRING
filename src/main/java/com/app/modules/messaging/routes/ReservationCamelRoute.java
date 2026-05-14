package com.app.modules.messaging.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ReservationCamelRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("direct:reservation.created")
                .routeId("route-reservation-created")
                .log("[Camel] CREATED event → ${body}")
                .to("log:com.app.messaging?level=INFO&showBody=true");

        from("direct:reservation.updated")
                .routeId("route-reservation-updated")
                .log("[Camel] UPDATED event → ${body}")
                .to("log:com.app.messaging?level=INFO&showBody=true");
    }
}
