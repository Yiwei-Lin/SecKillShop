package com.shop.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class MQConfig {
	
	public static final String SECKILL_QUEUE = "seckill.queue";

	public static final int CONCURRENT_COUNT = 10;

	@Bean
	public Queue queue() {
		return new Queue(SECKILL_QUEUE, true);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory seckillMQContainerFactory(
			SimpleRabbitListenerContainerFactoryConfigurer configurer,
			ConnectionFactory connectionFactory){
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConcurrentConsumers(CONCURRENT_COUNT);
		factory.setMaxConcurrentConsumers(CONCURRENT_COUNT);
		configurer.configure(factory, connectionFactory);
		return factory;
	}
}
