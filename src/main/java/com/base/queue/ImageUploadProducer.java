package com.base.queue;

import com.base.dto.request.ImageUploadBannerMessage;
import com.base.dto.request.ImageUploadChatMessage;
import com.base.dto.request.ImageUploadPostMessage;
import com.base.dto.request.ImageUploadProductMessage;
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

    @Value("${rabbitmq.routing-key.product-image-upload}")
    private String productRoutingKey;

    @Value("${rabbitmq.routing-key.banner-image-upload}")
    private String bannerRoutingKey;

    @Value("${rabbitmq.routing-key.post-image-upload}")
    private String postRoutingKey;

    @Value("${rabbitmq.routing-key.chat-image-upload}")
    private String chatRoutingKey;

    public void sendUploadProductMessage(ImageUploadProductMessage message) {
        try {
            amqpTemplate.convertAndSend(imageExchange, productRoutingKey, message);
            log.info("Queued image message: imageId={}, action={}",
                    message.getImageId(), message.getAction());

        } catch (AmqpConnectException | AmqpIOException e) {
            // RabbitMQ không khả dụng → fallback xử lý đồng bộ
            log.warn("RabbitMQ unavailable, falling back to sync upload. Reason: {}",
                    e.getMessage());
            fallbackService.process(message);
        }
    }

    public void sendUploadBannerMessage(ImageUploadBannerMessage message) {
        try {
            amqpTemplate.convertAndSend(imageExchange, bannerRoutingKey, message);
            log.info("Queued image message: bannerId={}, action={}",
                    message.getBannerId(), message.getAction());

        } catch (AmqpConnectException | AmqpIOException e) {
            // RabbitMQ không khả dụng → fallback xử lý đồng bộ
            log.warn("RabbitMQ unavailable, falling back to sync upload. Reason: {}",
                    e.getMessage());
            fallbackService.process(message);
        }
    }

    public void sendUploadPostMessage(ImageUploadPostMessage message) {
        try {
            amqpTemplate.convertAndSend(imageExchange, postRoutingKey, message);
            log.info("Queued image message: postId={}, action={}",
                    message.getPostId(), message.getAction());

        } catch (AmqpConnectException | AmqpIOException e) {
            // RabbitMQ không khả dụng → fallback xử lý đồng bộ
            log.warn("RabbitMQ unavailable, falling back to sync upload. Reason: {}",
                    e.getMessage());
            fallbackService.process(message);
        }
    }

    public void sendUploadChatMessage(ImageUploadChatMessage message) {
        try {
            amqpTemplate.convertAndSend(imageExchange, chatRoutingKey, message);
            log.info("Queued image message: messaegId={}, action={}",
                    message.getMessageId(), message.getAction());

        } catch (AmqpConnectException | AmqpIOException e) {
            // RabbitMQ không khả dụng → fallback xử lý đồng bộ
            log.warn("RabbitMQ unavailable, falling back to sync upload. Reason: {}",
                    e.getMessage());
            fallbackService.process(message);
        }
    }
}