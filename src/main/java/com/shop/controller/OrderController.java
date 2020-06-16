package com.shop.controller;

import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.message.ErrorMsg;
import com.shop.message.ResultMsg;
import com.shop.redis.RedisService;
import com.shop.service.GoodsService;
import com.shop.service.OrderService;
import com.shop.service.UserService;
import com.shop.vo.GoodsVo;
import com.shop.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	GoodsService goodsService;
	
    @GetMapping("/detail")
    public ResultMsg<OrderDetailVo> info(Model model, User user,
									  @RequestParam("orderId") long orderId) {
    	if(user == null) {
    		return ResultMsg.error(ErrorMsg.SESSION_ERROR);
    	}
    	Order order = orderService.getOrderById(orderId);
    	if(order == null) {
    		return ResultMsg.error(ErrorMsg.ORDER_NOT_EXIST);
    	}
    	long goodsId = order.getGoodsId();
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	OrderDetailVo vo = new OrderDetailVo(goods, order);
    	return ResultMsg.success(vo);
    }
    
}
