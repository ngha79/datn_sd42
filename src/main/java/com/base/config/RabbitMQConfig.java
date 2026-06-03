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

    @Value("${rabbitmq.queue.image-upload}")
    private String imageUploadQueue;

    @Value("${rabbitmq.exchange.image}")
    private String imageExchange;

    @Value("${rabbitmq.routing-key.image-upload}")
    private String imageUploadRoutingKey;

    @Bean
    public Queue imageUploadQueue() {
        return QueueBuilder.durable(imageUploadQueue).build();
    }

    @Bean
    public DirectExchange imageExchange() {
        return new DirectExchange(imageExchange);
    }

    @Bean
    public Binding imageUploadBinding() {
        return BindingBuilder
                .bind(imageUploadQueue())
                .to(imageExchange())
                .with(imageUploadRoutingKey);
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