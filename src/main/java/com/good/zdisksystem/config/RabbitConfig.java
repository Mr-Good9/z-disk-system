package com.good.zdisksystem.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 用于配置消息队列的交换机、队列和绑定关系
 */
@Configuration
public class RabbitConfig {
    
    /**
     * 创建聊天消息交换机
     * 使用直连交换机，根据routing key精确匹配队列
     */
    @Bean
    public Exchange chatExchange() {
        return ExchangeBuilder.directExchange("chat.exchange")
                .durable(true)  // 持久化
                .build();
    }
    
    /**
     * 创建聊天消息队列
     * 用于存储待处理的聊天消息
     */
    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable("chat.queue")  // 持久化队列
                .build();
    }
    
    /**
     * 绑定交换机和队列
     * 使用通配符routing key: chat.message.*
     * 匹配所有用户的消息路由
     */
    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue())
                .to(chatExchange())
                .with("chat.message.*")
                .noargs();
    }
} 