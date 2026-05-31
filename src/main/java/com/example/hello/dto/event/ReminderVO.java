package com.example.hello.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 提醒对象（与 reminder 表字段对应）。
 */
public class ReminderVO {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private Long id;
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
