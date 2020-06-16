package com.shop.dao;

import com.shop.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {

    @Select("select * from user where id=#{id}")
    public User getById(@Param("id") long id);
}
