package com.example.hello.service;

import com.example.hello.dto.reminder.UpcomingReminderVO;

import java.util.List;

/**
 * 提醒管理模块服务。
 */
public interface ReminderService {

    /**
     * 查询当前正在提醒中的提醒。
     *
     * @return 提醒列表（按 fire_time 升序）
     */
    List<UpcomingReminderVO> listUpcoming(int hours);

    /**
     * 将提醒标记为已查看。
     *
     * @param id reminder.id
     */
    void markRead(Long id);
}
