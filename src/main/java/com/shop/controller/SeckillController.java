package com.shop.controller;

import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.limit.Limit;
import com.shop.message.ErrorMsg;
import com.shop.message.MQMsg;
import com.shop.message.ResultMsg;
import com.shop.rabbitmq.MQSender;
import com.shop.redis.RedisService;
import com.shop.redis.prefix.GoodsPrefix;
import com.shop.service.GoodsService;
import com.shop.service.OrderService;
import com.shop.service.SeckillService;
import com.shop.service.UserService;
import com.shop.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	SeckillService seckillService;

	@Autowired
	MQSender sender;

	private HashSet<Long> localOverSet =  new HashSet<>();

	/**
	 *已弃用，改为定时任务，5分钟之内开始的秒杀商品提前放入redis。
	 */
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		List<GoodsVo> goodsList = goodsService.listGoodsVo();
//		if(goodsList == null) {
//			return;
//		}
//		for(GoodsVo goods : goodsList) {
//			redisService.set(GoodsPrefix.GOODS_STOCK_PREFIX, String.valueOf(goods.getId()), goods.getStockCount());
//			localOverMap.put(goods.getId(), false);
//		}
//	}

	@Limit(seconds = 1, maxCount = 5)
    @PostMapping("/do")
    public ResultMsg<Integer> doSeckill(Model model, User user,
							   @RequestParam("goodsId")long goodsId,
							   @RequestParam(value="verifyCode", defaultValue="0")String verifyCode) {
		model.addAttribute("user", user);
		if (user == null) {
			return ResultMsg.error(ErrorMsg.SESSION_ERROR);
		}
		boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
		if(!check) {
			return ResultMsg.error(ErrorMsg.VERIFY_CODE_ERROR);
		}
		// 判断内存标记
		boolean isOver = localOverSet.contains(goodsId);
		if (isOver) {
			return ResultMsg.error(ErrorMsg.SECKILL_OVER);
		}
		// 判断是否重复秒杀
		Order preOrder = orderService.getOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if (preOrder != null) {
			return ResultMsg.error(ErrorMsg.REPEATED_SECKILL);
		}

		// redis预减库存
		long stock = redisService.decr(GoodsPrefix.GOODS_STOCK_PREFIX, String.valueOf(goodsId));
		if (stock < 0) {
			localOverSet.add(goodsId);
			return ResultMsg.error(ErrorMsg.SECKILL_OVER);
		}

		// RabbitMQ 入队
		MQMsg msg = new MQMsg();
		msg.setUser(user);
		msg.setGoodsId(goodsId);
		sender.sendMsg(msg);
		return ResultMsg.success(0);  //排队
//    	// 执行秒杀操作
//    	Order order = seckillService.seckill(user, goods);
//        return ResultMsg.success(order);
	}

	/**
	 * 返回处理结果
	 * orderId：成功
	 * -1：秒杀失败
	 * 0： 排队中
	 */
	@GetMapping("/result")
	public ResultMsg<Long> seckillResult(Model model,User user,
									  @RequestParam("goodsId")long goodsId) throws ExecutionException, InterruptedException {
		model.addAttribute("user", user);
		if(user == null) {
			return ResultMsg.error(ErrorMsg.SESSION_ERROR);
		}
		long result  = seckillService.getSeckillResult(user.getId(), goodsId);
		return ResultMsg.success(result);
	}

	@PostMapping("/doTest")
	public String doSeckillTest(Model model, HttpServletResponse response,
								@RequestParam("token")String token,
								@RequestParam("goodsId")long goodsId) throws ExecutionException, InterruptedException {
    	User user = userService.getByToken(response, token);
		model.addAttribute("user", user);
		if(user == null) {
			return "login";
		}
		//判断库存
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goods.getStockCount();
		if(stock <= 0) {
			model.addAttribute("errmsg", ErrorMsg.SECKILL_OVER.getMessage());
			return "seckill_fail";
		}
		//判断是否已经秒杀到了
		Order preOrder = orderService.getOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if(preOrder != null) {
			model.addAttribute("errmsg", ErrorMsg.REPEATED_SECKILL.getMessage());
			return "seckill_fail";
		}
		//减库存 下订单 写入秒杀订单
		Order order = seckillService.seckill(user, goods);
		model.addAttribute("order", order);
		model.addAttribute("goods", goods);
		return "order_detail";
	}

	@GetMapping("/verifyCode")
	public ResultMsg<String> getVerifyCode(HttpServletResponse response,User user,
											  @RequestParam("goodsId")long goodsId) {
		if(user == null) {
			return ResultMsg.error(ErrorMsg.SESSION_ERROR);
		}
		BufferedImage image = null;
		OutputStream out = null;
		try {
			image  = seckillService.createVerifyCode(user, goodsId);
			out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			image.flush();
			return null;
		}catch(IOException e) {
			e.printStackTrace();
			return ResultMsg.error(ErrorMsg.SECKILL_FAIL);
		}
	}
}
