package com.shop.redis.prefix;

public interface KeyPrefix {

    int getExpireSeconds();
    String getPrefix();
}
