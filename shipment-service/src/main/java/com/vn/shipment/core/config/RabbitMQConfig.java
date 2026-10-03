package com.vn.shipment.core.config;

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

    @Value("${app.rabbitmq.exchange.shipment:shobbe.shipment.exchange}")
    private String shipmentExchange;

    @Value("${app.rabbitmq.exchange.dlx:shobbe.shipment.dlx}")
    private String shipmentDlx;

    @Value("${app.rabbitmq.queues.order-shipment.name:shobbe.order.shipment.queue}")
    private String orderShipmentQueue;

    @Value("${app.rabbitmq.queues.order-shipment.routing-key:shobbe.order.shipment.key}")
    private String orderShipmentRoutingKey;

    @Value("${app.rabbitmq.queues.order-shipment.dlq-name:shobbe.order.shipment.dlq}")
    private String orderShipmentDlqName;

    @Value("${app.rabbitmq.queues.order-shipment.dlq-routing-key:shobbe.order.shipment.dlq.key}")
    private String orderShipmentDlqRoutingKey;

    @Value("${app.rabbitmq.queues.status-updated.name:shobbe.shipment.status.queue}")
    private String statusUpdatedQueue;

    @Value("${app.rabbitmq.queues.status-updated.routing-key:shobbe.shipment.status.key}")
    private String statusUpdatedRoutingKey;

    @Value("${app.rabbitmq.queues.status-updated.dlq-name:shobbe.shipment.status.dlq}")
    private String statusUpdatedDlqName;

    @Value("${app.rabbitmq.queues.status-updated.dlq-routing-key:shobbe.shipment.status.dlq.key}")
    private String statusUpdatedDlqRoutingKey;

    // --- Main Exchanges ---

    @Bean
    public DirectExchange shipmentExchange() {
        return new DirectExchange(shipmentExchange, true, false);
    }

    @Bean
    public DirectExchange shipmentDeadLetterExchange() {
        return new DirectExchange(shipmentDlx, true, false);
    }

    // --- Order Shipment Queue & DLQ ---

    @Bean
    public Queue orderShipmentQueue() {
        return QueueBuilder.durable(orderShipmentQueue).build();
    }

    @Bean
    public Binding orderShipmentBinding(Queue orderShipmentQueue, DirectExchange shipmentExchange) {
        return BindingBuilder.bind(orderShipmentQueue).to(shipmentExchange).with(orderShipmentRoutingKey);
    }

    @Bean
    public Queue orderShipmentDeadLetterQueue() {
        return QueueBuilder.durable(orderShipmentDlqName).build();
    }

    @Bean
    public Binding orderShipmentDlqBinding(Queue orderShipmentDeadLetterQueue, DirectExchange shipmentDeadLetterExchange) {
        return BindingBuilder.bind(orderShipmentDeadLetterQueue).to(shipmentDeadLetterExchange).with(orderShipmentDlqRoutingKey);
    }

    // --- Status Updated Queue & DLQ ---

    @Bean
    public Queue statusUpdatedQueue() {
        return QueueBuilder.durable(statusUpdatedQueue).build();
    }

    @Bean
    public Binding statusUpdatedBinding(Queue statusUpdatedQueue, DirectExchange shipmentExchange) {
        return BindingBuilder.bind(statusUpdatedQueue).to(shipmentExchange).with(statusUpdatedRoutingKey);
    }

    @Bean
    public Queue statusUpdatedDeadLetterQueue() {
        return QueueBuilder.durable(statusUpdatedDlqName).build();
    }

    @Bean
    public Binding statusUpdatedDlqBinding(Queue statusUpdatedDeadLetterQueue, DirectExchange shipmentDeadLetterExchange) {
        return BindingBuilder.bind(statusUpdatedDeadLetterQueue).to(shipmentDeadLetterExchange).with(statusUpdatedDlqRoutingKey);
    }

    // --- Jackson Message Converter ---

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

    // --- Message Recoverer for DLQ republishing ---

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, shipmentDlx, orderShipmentDlqRoutingKey);
    }

    @Bean
    public RetryOperationsInterceptor retryOperationsInterceptor(MessageRecoverer messageRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(messageRecoverer)
                .build();
    }

    // --- Container Factory ---

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
