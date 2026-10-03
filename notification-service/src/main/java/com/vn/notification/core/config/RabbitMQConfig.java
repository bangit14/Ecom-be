package com.vn.notification.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.auth:shobbe.auth.exchange}")
    private String authExchange;

    @Value("${app.rabbitmq.exchange.dlx:shobbe.auth.dlx}")
    private String authDlx;

    @Value("${app.rabbitmq.queues.registration.name:shobbe.auth.registration.queue}")
    private String registrationQueue;

    @Value("${app.rabbitmq.queues.registration.routing-key:shobbe.auth.registration.key}")
    private String registrationRoutingKey;

    @Value("${app.rabbitmq.queues.registration.dlq-name:shobbe.auth.registration.dlq}")
    private String registrationDlqName;

    @Value("${app.rabbitmq.queues.registration.dlq-routing-key:shobbe.auth.registration.dlq.key}")
    private String registrationDlqRoutingKey;

    // --- Main Exchange, Queue & Binding ---

    @Bean
    public DirectExchange authExchange() {
        return new DirectExchange(authExchange, true, false);
    }

    @Bean
    public Queue registrationQueue() {
        return QueueBuilder.durable(registrationQueue).build();
    }

    @Bean
    public Binding registrationBinding(Queue registrationQueue, DirectExchange authExchange) {
        return BindingBuilder.bind(registrationQueue).to(authExchange).with(registrationRoutingKey);
    }

    // --- Dead Letter Exchange (DLX), DLQ & Binding ---

    @Bean
    public DirectExchange authDeadLetterExchange() {
        return new DirectExchange(authDlx, true, false);
    }

    @Bean
    public Queue registrationDeadLetterQueue() {
        return QueueBuilder.durable(registrationDlqName).build();
    }

    @Bean
    public Binding registrationDlqBinding(Queue registrationDeadLetterQueue, DirectExchange authDeadLetterExchange) {
        return BindingBuilder.bind(registrationDeadLetterQueue).to(authDeadLetterExchange).with(registrationDlqRoutingKey);
    }

    // --- Message Converter ---

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");
        converter.setClassMapper(typeMapper);
        return converter;
    }

    // --- RabbitTemplate ---

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    // --- Message Recoverer for DLQ routing on retry exhaustion ---

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, authDlx, registrationDlqRoutingKey);
    }

    @Bean
    public RetryOperationsInterceptor retryOperationsInterceptor(MessageRecoverer messageRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(messageRecoverer)
                .build();
    }

    // --- Container Factory with Retry & DLQ Interceptor ---

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RetryOperationsInterceptor retryOperationsInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(retryOperationsInterceptor);
        return factory;
    }
}
