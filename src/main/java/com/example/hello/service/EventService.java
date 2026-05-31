package com.example.hello.service;

import com.example.hello.dto.event.*;

import java.util.List;

/**
 * 事件管理模块服务。
 */
public interface EventService {

    CreateEventResponse create(CreateEventRequest request);

    EventListResponse list(String from, String to, String keyword, int page, int pageSize);

    EventVO getById(Long id);

    EventVO update(Long id, UpdateEventRequest request);

    void delete(Long id);

    void restore(Long id);

    List<ConflictEventVO> findConflicts(String startTime, String endTime, Long excludeId);
}
