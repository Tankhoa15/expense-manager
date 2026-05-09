package com.dev.expense_manager.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queues.transaction}")
    private String transactionQueue;

    @Value("${app.rabbitmq.queues.budget}")
    private String budgetQueue;

    @Value("${app.rabbitmq.queues.notification}")
    private String notificationQueue;

    @Value("${app.rabbitmq.routing-keys.transaction}")
    private String transactionRoutingKey;

    @Value("${app.rabbitmq.routing-keys.budget}")
    private String budgetRoutingKey;

    @Value("${app.rabbitmq.routing-keys.notification}")
    private String notificationRoutingKey;

    @Bean
    public DirectExchange expenseExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(transactionQueue)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", transactionRoutingKey + ".dlq")
                .build();
    }

    @Bean
    public Queue budgetQueue() {
        return QueueBuilder.durable(budgetQueue)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", budgetRoutingKey + ".dlq")
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", notificationRoutingKey + ".dlq")
                .build();
    }

    @Bean
    public Binding transactionBinding(Queue transactionQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(transactionQueue).to(expenseExchange).with(transactionRoutingKey);
    }

    @Bean
    public Binding budgetBinding(Queue budgetQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(budgetQueue).to(expenseExchange).with(budgetRoutingKey);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(notificationQueue).to(expenseExchange).with(notificationRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
