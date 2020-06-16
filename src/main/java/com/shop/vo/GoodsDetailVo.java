package com.shop.vo;

import com.shop.entity.User;

public class GoodsDetailVo {
	private GoodsVo goods ;
	private User user;
	private int remainSeconds = 0;
	private int seckillStatus = 0;

	public GoodsDetailVo(GoodsVo goods, User user, int remainSeconds, int seckillStatus) {
		this.goods = goods;
		this.user = user;
		this.remainSeconds = remainSeconds;
		this.seckillStatus = seckillStatus;
	}

	public GoodsVo getGoods() {
		return goods;
	}

	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getRemainSeconds() {
		return remainSeconds;
	}

	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}

	public int getSeckillStatus() {
		return seckillStatus;
	}

	public void setSeckillStatus(int seckillStatus) {
		this.seckillStatus = seckillStatus;
	}
}
