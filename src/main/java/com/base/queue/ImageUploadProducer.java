package com.base.queue;

import com.base.dto.request.ImageUploadMessage;
import com.base.service.impl.FallbackImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageUploadProducer {

    private final AmqpTemplate amqpTemplate;
    private final FallbackImageUploadService fallbackService;

    @Value("${rabbitmq.exchange.image}")
    private String imageExchange;

    @Value("${rabbitmq.routing-key.image-upload}")
    private String imageUploadRoutingKey;

    public void sendUploadMessage(ImageUploadMessage message) {
        try {
            amqpTemplate.convertAndSend(imageExchange, imageUploadRoutingKey, message);
            log.info("Queued image message: imageId={}, action={}",
                    message.getImageId(), message.getAction());

        } catch (AmqpConnectException | AmqpIOException e) {
            // RabbitMQ không khả dụng → fallback xử lý đồng bộ
            log.warn("RabbitMQ unavailable, falling back to sync upload. Reason: {}",
                    e.getMessage());
            fallbackService.process(message);
        }
    }
}