package com.shop.redis;

import com.shop.redis.prefix.KeyPrefix;
import com.shop.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

@Service
public class RedisService {

    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 获取对象
     * */
    public Object get(KeyPrefix prefix, String key, Class<?> clazz) {
        //生成真正的key
        String realKey  = prefix.getPrefix() + key;
        String str = jedisCluster.get(realKey);
        return RedisUtil.strToObj(str, clazz);
    }

    /**
     * 获取str
     * */
    public String get(KeyPrefix prefix, String key) {
        //生成真正的key
        String realKey  = prefix.getPrefix() + key;
        return jedisCluster.get(realKey);
        }

    /**
     * 设置对象
     * */
    public boolean set(KeyPrefix prefix, String key, Object value) {
        String str;
        if(value instanceof String) str = (String) value;
        else str = RedisUtil.objToStr(value);
        if (str == null || str.length() <= 0) {
            return false;
        }
        String realKey = prefix.getPrefix() + key;
        int seconds = prefix.getExpireSeconds();
        if (seconds <= 0) {
            jedisCluster.set(realKey, str);
        } else {
            jedisCluster.setex(realKey, seconds, str);
        }
        return true;
    }

    /**
     * 删除key
     * */
    public boolean delete(KeyPrefix prefix, String key) {
        //生成真正的key
        String realKey  = prefix.getPrefix() + key;
        return jedisCluster.del(realKey) > 0;
    }

    /**
     * 判断key是否存在
     * */
    public boolean exists(KeyPrefix prefix, String key) {
        return  jedisCluster.exists(prefix.getPrefix() + key);
    }

    /**
     * 增加值
     * */
    public Long incr(KeyPrefix prefix, String key) {
        return jedisCluster.incr(prefix.getPrefix() + key);
    }

    /**
     * 减少值
     * */
    public Long decr(KeyPrefix prefix, String key) {
        return  jedisCluster.decr(prefix.getPrefix() + key);
    }
}
