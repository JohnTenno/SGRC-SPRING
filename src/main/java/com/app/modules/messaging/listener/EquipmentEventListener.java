package com.app.modules.messaging.listener;

import com.app.modules.messaging.config.RabbitMQConfig;
import org.apache.camel.ProducerTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EquipmentEventListener {

    @Autowired
    private ProducerTemplate producerTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EQUIPMENT_CREATED)
    public void onCreated(String message) {
        producerTemplate.sendBody("direct:equipment.created", message);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EQUIPMENT_UPDATED)
    public void onUpdated(String message) {
        producerTemplate.sendBody("direct:equipment.updated", message);
    }
}
