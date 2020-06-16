package com.shop.redis.prefix;

public class GoodsPrefix extends BasePrefix {

	private static final int GOODS_EXPIRE_SECONDS = 60;

	private GoodsPrefix(String prefix, int expireSeconds) {
		super(prefix, expireSeconds);
	}

	public static final GoodsPrefix GOODS_LIST_PREFIX = new GoodsPrefix("gl", GOODS_EXPIRE_SECONDS);
	public static final GoodsPrefix GOODS_STOCK_PREFIX = new GoodsPrefix("gs", 0);
}
