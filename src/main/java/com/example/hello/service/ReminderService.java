package com.example.hello.service;

import com.example.hello.dto.reminder.UpcomingReminderVO;

import java.util.List;

/**
 * 提醒管理模块服务。
 */
public interface ReminderService {

    /**
     * 查询未来若干小时内即将到来的提醒。
     *
     * @param hours 未来小时数，默认 24
     * @return 提醒列表（按 fire_time 升序）
     */
    List<UpcomingReminderVO> listUpcoming(int hours);
}
