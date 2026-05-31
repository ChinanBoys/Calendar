package com.example.hello.controller;

import com.example.hello.common.Result;
import com.example.hello.dto.reminder.UpcomingReminderVO;
import com.example.hello.service.ReminderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提醒管理模块。见 05-接口文档.md 第 3 节。
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 3.1 查询即将到来的提醒。
     * reminder 联表 event，返回未来 N 小时内将触发的提醒。
     *
     * @param hours 未来小时数，默认 24
     */
    @GetMapping("/upcoming")
    public Result<List<UpcomingReminderVO>> upcoming(
            @RequestParam(defaultValue = "24") int hours) {
        return Result.success(reminderService.listUpcoming(hours));
    }
}
