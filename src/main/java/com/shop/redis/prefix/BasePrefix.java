package com.shop.redis.prefix;

public abstract class BasePrefix implements KeyPrefix {

	private String prefix;
	private int expireSeconds;

	public BasePrefix(String prefix, int expireSeconds) {
		this.prefix = prefix;
		this.expireSeconds = expireSeconds;
	}

	public BasePrefix(String prefix) {
		this(prefix, 0);   // 永不过期的key
	}

	public int getExpireSeconds() {
		return expireSeconds;
	}

	public String getPrefix() {
		String className = getClass().getSimpleName();
		return className.replace("Prefix", "")+ ":" + prefix + ":";
	}
}
