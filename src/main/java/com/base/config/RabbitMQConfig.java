package com.base.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.product-image-upload}")
    private String productQueue;

    @Value("${rabbitmq.queue.banner-image-upload}")
    private String bannerQueue;

    @Value("${rabbitmq.queue.post-image-upload}")
    private String postQueue;

    @Value("${rabbitmq.queue.chat-image-upload}")
    private String chatQueue;

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

    @Bean
    public Queue imageProductUploadQueue() {
        return QueueBuilder.durable(productQueue).build();
    }

    @Bean
    public Binding imageProductUploadBinding() {
        return BindingBuilder
                .bind(imageProductUploadQueue())
                .to(imageExchange())
                .with(productRoutingKey);
    }

    @Bean
    public Queue imageBannerUploadQueue() {
        return QueueBuilder.durable(bannerQueue).build();
    }

    @Bean
    public Binding imageBannerUploadBinding() {
        return BindingBuilder
                .bind(imageBannerUploadQueue())
                .to(imageExchange())
                .with(bannerRoutingKey);
    }

    @Bean
    public Queue imagePostUploadQueue() {
        return QueueBuilder.durable(postQueue).build();
    }

    @Bean
    public Binding imageUploadBinding() {
        return BindingBuilder
                .bind(imagePostUploadQueue())
                .to(imageExchange())
                .with(postRoutingKey);
    }

    @Bean
    public Queue imageChatUploadQueue() {
        return QueueBuilder.durable(chatQueue).build();
    }

    @Bean
    public Binding imageChatUploadBinding() {
        return BindingBuilder
                .bind(imageChatUploadQueue())
                .to(imageExchange())
                .with(chatRoutingKey);
    }

    @Bean
    public DirectExchange imageExchange() {
        return new DirectExchange(imageExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        rabbitTemplate.setChannelTransacted(false);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setMissingQueuesFatal(false); // ← key setting
        return factory;
    }
}