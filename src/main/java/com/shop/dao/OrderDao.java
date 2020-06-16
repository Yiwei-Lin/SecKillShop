package com.shop.dao;

import com.shop.entity.Order;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {
	
	@Select("select * from `order` where user_id=#{userId} and goods_id=#{goodsId}")
	public Order getByUserIdAndGoodsId(@Param("userId") long userId, @Param("goodsId") long goodsId);

	@Insert("insert into `order`(user_id, goods_id, goods_name, goods_count, goods_price, status, create_date)values("
			+ "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{status}, #{createDate} )")
	@SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
	public long insertByOrder(Order order);

	@Select("select * from `order` where id = #{orderId}")
	public Order getOrderById(@Param("orderId")long orderId);
}
