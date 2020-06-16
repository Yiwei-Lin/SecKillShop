package com.shop.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * redis配置类
 */
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfig {

    private String clusterNodes;
    private int poolMaxTotal;
    private int poolMaxIdle;
    private int poolMaxWait;

    public void setClusterNodes(String clusterNodes) { this.clusterNodes = clusterNodes; }

    public void setPoolMaxTotal(int poolMaxTotal) {
        this.poolMaxTotal = poolMaxTotal;
    }

    public void setPoolMaxIdle(int poolMaxIdle) {
        this.poolMaxIdle = poolMaxIdle;
    }

    public void setPoolMaxWait(int poolMaxWait) {
        this.poolMaxWait = poolMaxWait;
    }

    @Bean
    public JedisCluster jedisCluster(){
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(poolMaxTotal);
        config.setMaxIdle(poolMaxIdle);
        config.setMaxWaitMillis(poolMaxWait);
        String[] nodes = clusterNodes.split(",");
        Set<HostAndPort> nodesSet =new HashSet<>();
        for (String node:nodes) {
            String[] hp = node.split(":");
            nodesSet.add(new HostAndPort(hp[0],Integer.parseInt(hp[1])));
        }
        return new JedisCluster(nodesSet, config);
    }
}
