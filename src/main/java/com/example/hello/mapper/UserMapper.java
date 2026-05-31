package com.example.hello.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 查询用户默认提醒提前分钟数；用户不存在时返回 null。
     */
    Integer selectDefaultReminderMinutes(@Param("userId") Long userId);
}
