package com.example.hello.service.impl;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.dto.event.*;
import com.example.hello.entity.Event;
import com.example.hello.entity.Reminder;
import com.example.hello.mapper.EventMapper;
import com.example.hello.mapper.ReminderMapper;
import com.example.hello.mapper.UserMapper;
import com.example.hello.service.EventService;
import com.example.hello.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class EventServiceImpl implements EventService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_DELETED = 0;
    private static final int DEFAULT_REMINDER_MINUTES = 15;

    private final EventMapper eventMapper;
    private final ReminderMapper reminderMapper;
    private final UserMapper userMapper;
    private final VoiceCalProperties properties;

    public EventServiceImpl(EventMapper eventMapper,
                            ReminderMapper reminderMapper,
                            UserMapper userMapper,
                            VoiceCalProperties properties) {
        this.eventMapper = eventMapper;
        this.reminderMapper = reminderMapper;
        this.userMapper = userMapper;
        this.properties = properties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateEventResponse create(CreateEventRequest request) {
        Long userId = currentUserId();
        LocalDateTime start = DateTimeUtil.parse(request.getStartTime());
        LocalDateTime end = DateTimeUtil.parse(request.getEndTime());
        validateTimeRange(start, end);

        boolean force = Boolean.TRUE.equals(request.getForce());
        List<ConflictEventVO> conflicts = eventMapper.selectConflicts(userId, start, end, null);

        CreateEventResponse response = new CreateEventResponse();
        if (!conflicts.isEmpty() && !force) {
            response.setCreated(false);
            response.setConflicts(conflicts);
            return response;
        }

        Event event = buildEventForCreate(userId, request, start, end);
        eventMapper.insert(event);

        List<Integer> offsets = resolveReminderOffsets(request.getReminderOffsets(), userId);
        insertReminders(event, offsets);

        response.setCreated(true);
        response.setEvent(toEventVO(event));
        return response;
    }

    @Override
    public EventListResponse list(String from, String to, String keyword, int page, int pageSize) {
        Long userId = currentUserId();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);

        EventQueryParam param = new EventQueryParam();
        param.setUserId(userId);
        if (from != null && !from.isBlank()) {
            param.setFrom(DateTimeUtil.parse(from));
        }
        if (to != null && !to.isBlank()) {
            param.setTo(DateTimeUtil.parse(to));
        }
        param.setKeyword(keyword);
        param.setOffset((safePage - 1) * safeSize);
        param.setPageSize(safeSize);

        long total = eventMapper.countByQuery(param);
        List<Event> events = eventMapper.selectPageByQuery(param);

        EventListResponse response = new EventListResponse();
        response.setTotal(total);
        response.setRows(events.stream().map(this::toEventVO).toList());
        return response;
    }

    @Override
    public EventVO getById(Long id) {
        Event event = requireActiveEvent(id);
        EventVO vo = toEventVO(event);
        vo.setReminders(reminderMapper.selectByEventId(id).stream()
                .map(this::toReminderVO)
                .toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventVO update(Long id, UpdateEventRequest request) {
        Event existing = requireActiveEvent(id);
        Long userId = currentUserId();

        LocalDateTime start = request.getStartTime() != null
                ? DateTimeUtil.parse(request.getStartTime()) : existing.getStartTime();
        LocalDateTime end = request.getEndTime() != null
                ? DateTimeUtil.parse(request.getEndTime()) : existing.getEndTime();
        validateTimeRange(start, end);

        boolean force = Boolean.TRUE.equals(request.getForce());
        List<ConflictEventVO> conflicts = eventMapper.selectConflicts(userId, start, end, id);
        if (!conflicts.isEmpty() && !force) {
            throw new EventConflictException(conflicts);
        }

        Event patch = new Event();
        patch.setId(id);
        patch.setUserId(userId);
        if (request.getTitle() != null) {
            patch.setTitle(request.getTitle());
        }
        if (request.getStartTime() != null) {
            patch.setStartTime(start);
        }
        if (request.getEndTime() != null) {
            patch.setEndTime(end);
        }
        if (request.getAllDay() != null) {
            patch.setAllDay(request.getAllDay());
        }
        if (request.getLocation() != null) {
            patch.setLocation(request.getLocation());
        }
        if (request.getNote() != null) {
            patch.setNote(request.getNote());
        }
        if (request.getRecurrence() != null) {
            patch.setRecurrence(request.getRecurrence());
        }
        patch.setUpdateTime(LocalDateTime.now());
        eventMapper.updateSelective(patch);

        Event updated = eventMapper.selectByIdAndUserId(id, userId);
        LocalDateTime eventStart = updated.getStartTime();

        if (request.getReminderOffsets() != null) {
            reminderMapper.deleteByEventId(id);
            insertReminders(updated, request.getReminderOffsets());
        } else if (request.getStartTime() != null) {
            // 时间变更：按现有提醒 offset 重算 fire_time
            recalculateReminderFireTimes(id, eventStart);
        }

        return toEventVO(eventMapper.selectByIdAndUserId(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireActiveEvent(id);
        int rows = eventMapper.updateStatus(id, currentUserId(), STATUS_DELETED);
        if (rows == 0) {
            throw new EventNotFoundException("事件不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        Long userId = currentUserId();
        Event event = eventMapper.selectByIdAndUserId(id, userId);
        if (event == null) {
            throw new EventNotFoundException("事件不存在");
        }
        eventMapper.updateStatus(id, userId, STATUS_ACTIVE);
    }

    @Override
    public List<ConflictEventVO> findConflicts(String startTime, String endTime, Long excludeId) {
        LocalDateTime start = DateTimeUtil.parse(startTime);
        LocalDateTime end = DateTimeUtil.parse(endTime);
        validateTimeRange(start, end);
        return eventMapper.selectConflicts(currentUserId(), start, end, excludeId);
    }

    private Event buildEventForCreate(Long userId, CreateEventRequest request,
                                      LocalDateTime start, LocalDateTime end) {
        Event event = new Event();
        event.setUserId(userId);
        event.setTitle(request.getTitle());
        event.setStartTime(start);
        event.setEndTime(end);
        event.setAllDay(Boolean.TRUE.equals(request.getAllDay()));
        event.setLocation(request.getLocation());
        event.setNote(request.getNote());
        event.setRecurrence(
                request.getRecurrence() != null && !request.getRecurrence().isBlank()
                        ? request.getRecurrence() : "none");
        event.setStatus(STATUS_ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        event.setCreateTime(now);
        event.setUpdateTime(now);
        return event;
    }

    private void insertReminders(Event event, List<Integer> offsets) {
        if (offsets == null || offsets.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Integer offset : offsets) {
            if (offset == null || offset < 0) {
                continue;
            }
            Reminder reminder = new Reminder();
            reminder.setEventId(event.getId());
            reminder.setUserId(event.getUserId());
            reminder.setOffsetMinutes(offset);
            reminder.setFireTime(event.getStartTime().minusMinutes(offset));
            reminder.setSent(false);
            reminder.setCreateTime(now);
            reminderMapper.insert(reminder);
        }
    }

    private void recalculateReminderFireTimes(Long eventId, LocalDateTime startTime) {
        List<Reminder> reminders = reminderMapper.selectByEventId(eventId);
        if (reminders.isEmpty()) {
            return;
        }
        reminderMapper.deleteByEventId(eventId);
        LocalDateTime now = LocalDateTime.now();
        for (Reminder item : reminders) {
            Reminder fresh = new Reminder();
            fresh.setEventId(item.getEventId());
            fresh.setUserId(item.getUserId());
            fresh.setOffsetMinutes(item.getOffsetMinutes());
            fresh.setFireTime(startTime.minusMinutes(item.getOffsetMinutes()));
            fresh.setSent(item.getSent() != null && item.getSent());
            fresh.setCreateTime(now);
            reminderMapper.insert(fresh);
        }
    }

    private List<Integer> resolveReminderOffsets(List<Integer> requestOffsets, Long userId) {
        if (requestOffsets != null && !requestOffsets.isEmpty()) {
            return requestOffsets;
        }
        Integer defaultMinutes = userMapper.selectDefaultReminderMinutes(userId);
        int minutes = defaultMinutes != null ? defaultMinutes : DEFAULT_REMINDER_MINUTES;
        return List.of(minutes);
    }

    private Event requireActiveEvent(Long id) {
        Event event = eventMapper.selectByIdAndUserId(id, currentUserId());
        if (event == null || !Objects.equals(event.getStatus(), STATUS_ACTIVE)) {
            throw new EventNotFoundException("事件不存在");
        }
        return event;
    }

    private Long currentUserId() {
        return properties.getDefaultUserId();
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("开始/结束时间不能为空");
        }
        if (!end.isAfter(start) && !end.isEqual(start)) {
            throw new IllegalArgumentException("结束时间必须大于或等于开始时间");
        }
    }

    private EventVO toEventVO(Event event) {
        EventVO vo = new EventVO();
        vo.setId(event.getId());
        vo.setUserId(event.getUserId());
        vo.setTitle(event.getTitle());
        vo.setStartTime(event.getStartTime());
        vo.setEndTime(event.getEndTime());
        vo.setAllDay(event.getAllDay());
        vo.setLocation(event.getLocation());
        vo.setNote(event.getNote());
        vo.setRecurrence(event.getRecurrence());
        vo.setStatus(event.getStatus());
        vo.setCreateTime(event.getCreateTime());
        vo.setUpdateTime(event.getUpdateTime());
        return vo;
    }

    private ReminderVO toReminderVO(Reminder reminder) {
        ReminderVO vo = new ReminderVO();
        vo.setId(reminder.getId());
        vo.setOffsetMinutes(reminder.getOffsetMinutes());
        vo.setFireTime(reminder.getFireTime());
        vo.setSent(reminder.getSent());
        return vo;
    }

    /** 事件不存在 */
    public static class EventNotFoundException extends RuntimeException {
        public EventNotFoundException(String message) {
            super(message);
        }
    }

    /** 更新时时间冲突 */
    public static class EventConflictException extends RuntimeException {
        private final List<ConflictEventVO> conflicts;

        public EventConflictException(List<ConflictEventVO> conflicts) {
            super("时间冲突");
            this.conflicts = conflicts != null ? conflicts : Collections.emptyList();
        }

        public List<ConflictEventVO> getConflicts() {
            return conflicts;
        }
    }
}
