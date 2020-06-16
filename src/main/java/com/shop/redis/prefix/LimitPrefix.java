package com.shop.redis.prefix;

public class LimitPrefix extends BasePrefix{

	private LimitPrefix(String prefix, int expireSeconds) { super(prefix, expireSeconds); }
	
	public static LimitPrefix expireLimit(int expireSeconds) { return new LimitPrefix("lmt", expireSeconds); }

}
