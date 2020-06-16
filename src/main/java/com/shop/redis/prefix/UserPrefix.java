package com.shop.redis.prefix;

public class UserPrefix extends BasePrefix{

	private static final int TOKEN_EXPIRE_SECONDS = 3600;
	private UserPrefix(String prefix, int expireSeconds) {
		super(prefix, expireSeconds);
	}

	public static final UserPrefix TOKEN_PREFIX = new UserPrefix("tk", TOKEN_EXPIRE_SECONDS);
	public static final UserPrefix ID_PREFIX = new UserPrefix("id", 0);
}
