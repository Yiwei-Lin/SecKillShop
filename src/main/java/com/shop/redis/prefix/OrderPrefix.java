package com.shop.redis.prefix;

public class OrderPrefix extends BasePrefix {

	private static final int ORDER_EXPIRE_SECONDS = 3600;

	public OrderPrefix(String prefix, int expireSeconds) {
		super(prefix, expireSeconds);
	}

	public static final OrderPrefix ORDER_PREFIX = new OrderPrefix("od", ORDER_EXPIRE_SECONDS);
}
