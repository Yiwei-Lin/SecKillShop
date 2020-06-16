package com.shop.service;

import com.shop.dao.OrderDao;
import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.redis.RedisService;
import com.shop.redis.prefix.OrderPrefix;
import com.shop.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
	
	@Autowired
	OrderDao orderDao;

	@Autowired
	RedisService redisService;
	
	public Order getOrderByUserIdAndGoodsId(long userId, long goodsId) {
		return (Order) redisService.get(OrderPrefix.ORDER_PREFIX, userId + "_" + goodsId, Order.class);
	}

	public Order getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}


	@Transactional
	public Order createOrder(User user, GoodsVo goods) {
		Order order = new Order();
		order.setCreateDate(new Date());
		order.setGoodsCount(1);
		order.setGoodsId(goods.getId());
		order.setGoodsName(goods.getGoodsName());
		order.setGoodsPrice(goods.getSeckillPrice());
		order.setStatus(0);
		order.setUserId(user.getId());
		orderDao.insertByOrder(order);
		redisService.set(OrderPrefix.ORDER_PREFIX, user.getId() + "_" + goods.getId(), order);
		return order;
	}
}
