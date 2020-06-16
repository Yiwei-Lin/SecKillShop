package com.shop.service;

import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.redis.RedisService;
import com.shop.redis.prefix.SeckillPrefix;
import com.shop.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class SeckillService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;

	@Autowired
	RedisService redisService;

	@Transactional
	public Order seckill(User user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		boolean isSuccess = goodsService.reduceStock(goods);
		// 生成订单
		if(isSuccess)
			return orderService.createOrder(user, goods);
		setGoodsOver(goods.getId());
		return null;
	}

	public long getSeckillResult(Long userId, long goodsId) {
		Order order = orderService.getOrderByUserIdAndGoodsId(userId, goodsId);
		if(order != null) {			//秒杀成功
			return order.getId();
		}else {
			boolean isOver = getGoodsOver(goodsId);
			if(isOver) {
				return -1;
			}else {
				return 0;
			}
		}
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(SeckillPrefix.SECKILL_OVER_PREFIX, String.valueOf(goodsId), true);
	}

	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(SeckillPrefix.SECKILL_OVER_PREFIX, String.valueOf(goodsId));
	}

	public BufferedImage createVerifyCode(User user, long goodsId) {
		if(user == null || goodsId <=0) {
			return null;
		}
		int width = 80;
		int height = 32;
		// 生成图片
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// 设置背景色
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// 画边框
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		Random randGenerator = new Random();
		for (int i = 0; i < 50; i++) {
			int x = randGenerator.nextInt(width);
			int y = randGenerator.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// 随机生成验证码
		String verifyCode = generateVerifyCode(randGenerator);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("DejaVuSans", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();   // 及时销毁Graphics对象，否则内存泄漏
		// 把验证码存到redis中
		redisService.set(SeckillPrefix.VERIFY_CODE_PREFIX, user.getId() + "_" + goodsId, String.valueOf(calc(verifyCode)));
		return image;
	}

	public boolean checkVerifyCode(User user, long goodsId, String verifyCode) {
		if(user == null || goodsId <=0) {
			return false;
		}
		String codeOld =  redisService.get(SeckillPrefix.VERIFY_CODE_PREFIX, user.getId() + "_" + goodsId);
		if(codeOld == null || !codeOld.equals(verifyCode)) {
			return false;
		}
		redisService.delete(SeckillPrefix.VERIFY_CODE_PREFIX, user.getId() + "_" + goodsId);
		return true;
	}

	private static int calc(String exp) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (int) engine.eval(exp);
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private String generateVerifyCode(Random randGenerator) {
		int num1 = randGenerator.nextInt(10);
		int num2 = randGenerator.nextInt(10);
		int num3 = randGenerator.nextInt(10);
		char[] ops = new char[] {'+', '-', '*'};
		char op1 = ops[randGenerator.nextInt(3)];
		char op2 = ops[randGenerator.nextInt(3)];
		return "" + num1 + op1 + num2 + op2 + num3;
	}

}
