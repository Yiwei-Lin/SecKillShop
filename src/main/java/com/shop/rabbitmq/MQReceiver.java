package com.shop.rabbitmq;

import com.shop.config.MQConfig;
import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.message.MQMsg;
import com.shop.redis.RedisService;
import com.shop.service.GoodsService;
import com.shop.service.OrderService;
import com.shop.service.SeckillService;
import com.shop.utils.RedisUtil;
import com.shop.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQReceiver {

	private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	SeckillService seckillService;

	@RabbitListener(queues = {MQConfig.SECKILL_QUEUE}, containerFactory = "seckillMQContainerFactory")
	public void receive(String strMsg) {
		logger.info("receive message:" + strMsg);
		MQMsg msg = (MQMsg) RedisUtil.strToObj(strMsg, MQMsg.class);
		User user = msg.getUser();
		long goodsId = msg.getGoodsId();

		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goods.getStockCount();
		if (stock <= 0) {
			return;
		}
		// 判断是否已经秒杀到了
		Order order = orderService.getOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if (order != null) {
			return;
		}
		//减库存 下订单 写入秒杀订单
		seckillService.seckill(user, goods);
	}
}
