package com.shop.config;

import com.shop.redis.RedisService;
import com.shop.redis.prefix.GoodsPrefix;
import com.shop.service.GoodsService;
import com.shop.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

/**
 * 定时任务配置类：
 *    优化：
 *      定时检查5分钟之内开始的秒杀商品，加入redis中进行缓存预热
 */
@Configuration
@ConfigurationProperties(prefix = "schedule")
@EnableScheduling
public class ScheduleTaskConfig {

    private static final int SCAN_GOODS_RATE = 60 * 1000;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RedisService redisService;

    @Scheduled(fixedRate = SCAN_GOODS_RATE)
    public void scanGoodsToRedis() {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        Date curTime = new Date();
        for (GoodsVo goods : goodsList) {
            if(redisService.get(GoodsPrefix.GOODS_STOCK_PREFIX, String.valueOf(goods.getId())) != null)  // 已在缓存
                continue;
            if (goods.getStartDate().getTime() - curTime.getTime() <= 5 * 60 * 1000) {   // 不在缓存且距离开始时间小于5分钟
                redisService.set(GoodsPrefix.GOODS_STOCK_PREFIX, String.valueOf(goods.getId()), goods.getStockCount());
            }
        }

    }
}
