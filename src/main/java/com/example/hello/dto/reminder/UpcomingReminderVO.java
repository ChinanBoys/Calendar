package com.example.hello.dto.reminder;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * GET /api/reminders/upcoming 响应列表项。
 * 见 05-接口文档.md 3.1.3（reminder + event 联表字段）。
 */
public class UpcomingReminderVO {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private Long id;
    private Long eventId;
    private String eventTitle;

    @JsonFormat(pattern = PATTERN)
    private LocalDateTime startTime;

    private Integer offsetMinutes;

    @JsonFormat(pattern = PATTERN)
    private LocalDateTime fireTime;

    private Boolean sent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getOffsetMinutes() {
        return offsetMinutes;
    }

    public void setOffsetMinutes(Integer offsetMinutes) {
        this.offsetMinutes = offsetMinutes;
    }

    public LocalDateTime getFireTime() {
        return fireTime;
    }

    public void setFireTime(LocalDateTime fireTime) {
        this.fireTime = fireTime;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }
}
