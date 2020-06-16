package com.shop.message;

import com.shop.entity.User;

/**
 * MQ消息：
 *   包括用户信息和秒杀的商品id
 */
public class MQMsg {
    private User user;
    private long goodsId;
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public long getGoodsId() {
        return goodsId;
    }
    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }

}
