package com.shop.vo;

import com.shop.entity.Order;

public class OrderDetailVo {
	private GoodsVo goods;
	private Order order;
	public GoodsVo getGoods() {
		return goods;
	}
	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}

	public OrderDetailVo(GoodsVo goods, Order order) {
		this.goods = goods;
		this.order = order;
	}
}
