package com.shop.redis.prefix;

public class SeckillPrefix extends BasePrefix {

	private static final int VERIFY_CODE_EXPIRE_SECONDS = 60;

	private SeckillPrefix(String prefix, int expireSeconds) { super(prefix, expireSeconds); }
	public static final SeckillPrefix SECKILL_OVER_PREFIX = new SeckillPrefix("so", 0);
	public static final SeckillPrefix VERIFY_CODE_PREFIX = new SeckillPrefix("vc", VERIFY_CODE_EXPIRE_SECONDS);
}
