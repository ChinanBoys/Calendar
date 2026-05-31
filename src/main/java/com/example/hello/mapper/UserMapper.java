package com.example.hello.mapper;

import com.example.hello.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 查询用户默认提醒提前分钟数；用户不存在时返回 null。
     */
    Integer selectDefaultReminderMinutes(@Param("userId") Long userId);

    /**
     * 查询用户偏好设置；用户不存在时返回 null。
     */
    UserSettings selectSettingsByUserId(@Param("userId") Long userId);

    /**
     * 部分更新用户偏好设置。
     */
    int updateSettingsSelective(@Param("userId") Long userId, @Param("settings") UserSettings settings);
}
