package com.example.hello.controller;

import com.example.hello.common.Result;
import com.example.hello.dto.reminder.UpcomingReminderVO;
import com.example.hello.service.ReminderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
     * 3.1 查询正在提醒中的提醒。
     * reminder 联表 event，返回 fire_time 到 end_time 之间的提醒。
     *
     * @param hours 兼容旧调用，当前提醒定义不再使用未来窗口
     */
    @GetMapping("/upcoming")
    public Result<List<UpcomingReminderVO>> upcoming(
            @RequestParam(defaultValue = "24") int hours) {
        return Result.success(reminderService.listUpcoming(hours));
    }

    /**
     * 3.2 将提醒标记为已查看。
     */
    @PatchMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        return markReadByPost(id);
    }

    @PostMapping("/{id}/read")
    public Result<Void> markReadByPost(@PathVariable Long id) {
        reminderService.markRead(id);
        return Result.success();
    }
}
