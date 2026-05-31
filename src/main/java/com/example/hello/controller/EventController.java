package com.example.hello.controller;

import com.example.hello.common.Result;
import com.example.hello.dto.event.*;
import com.example.hello.service.EventService;
import com.example.hello.service.impl.EventServiceImpl;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 事件管理模块。见 05-接口文档.md 第 2 节。
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** 2.1 创建事件 */
    @PostMapping
    public Result<CreateEventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        CreateEventResponse data = eventService.create(request);
        if (Boolean.FALSE.equals(data.getCreated())) {
            return Result.success("时间冲突", data);
        }
        return Result.success(data);
    }

    /** 2.2 事件列表查询 */
    @GetMapping
    public Result<EventListResponse> list(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(eventService.list(from, to, keyword, page, pageSize));
    }

    /** 2.7 冲突检测（须在 /{id} 之前声明） */
    @GetMapping("/conflicts")
    public Result<List<ConflictEventVO>> conflicts(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) Long excludeId) {
        return Result.success(eventService.findConflicts(startTime, endTime, excludeId));
    }

    /** 2.3 根据 ID 查询事件详情 */
    @GetMapping("/{id}")
    public Result<EventVO> getById(@PathVariable Long id) {
        return Result.success(eventService.getById(id));
    }

    /** 2.4 修改事件 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody UpdateEventRequest request) {
        try {
            return Result.success(eventService.update(id, request));
        } catch (EventServiceImpl.EventConflictException ex) {
            return Result.success("时间冲突", ex.getConflicts());
        }
    }

    /** 2.5 删除事件（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return Result.success();
    }

    /** 2.6 撤销删除 */
    @PutMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        eventService.restore(id);
        return Result.success();
    }
}
