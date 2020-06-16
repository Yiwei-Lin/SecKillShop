package com.shop.dao;

import com.shop.entity.Goods;
import com.shop.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface GoodsDao {
	
	@Select("select g.*,skg.stock_count, skg.start_date, skg.end_date,skg.seckill_price from seckill_goods skg left join goods g on skg.goods_id = g.id")
	public List<GoodsVo> listGoodsVo();

	@Select("select g.*,skg.stock_count, skg.start_date, skg.end_date,skg.seckill_price from seckill_goods skg left join goods g on skg.goods_id = g.id where g.id = #{goodsId}")
	public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

	@Update("update seckill_goods set stock_count = stock_count - 1 where goods_id = #{id} and stock_count > 0")
	public int reduceStock(Goods g);
	
}
