package com.shop.rabbitmq;

import com.shop.config.MQConfig;
import com.shop.message.MQMsg;
import com.shop.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQSender {

	private static Logger logger = LoggerFactory.getLogger(MQSender.class);

	@Autowired
	RabbitTemplate rabbitTemplate;
	
	public void sendMsg(MQMsg msg) {
		String strMsg = RedisUtil.objToStr(msg);
		logger.info("send message:" + msg);
		rabbitTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, strMsg);
	}

}
