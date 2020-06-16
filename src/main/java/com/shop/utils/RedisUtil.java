package com.shop.utils;

import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;

/**
 * redis工具类
 */
public class RedisUtil {

    public static String objToStr(Object value) {
        if(value == null) return null;
        return JSON.toJSONString(value);
    }

    public static Object strToObj(String str, Class<?> clazz) {
        if(str == null || clazz == null || str.length() <= 0) return null;
        return JSON.toJavaObject(JSON.parseObject(str), clazz);
    }

    public static void closeJedis(Jedis jedis){
        if(jedis == null) return;
        jedis.close();
    }
}
