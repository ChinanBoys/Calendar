package com.example.hello.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 冲突检测返回的精简事件信息。
 */
public class ConflictEventVO {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private Long id;
    private String title;

    @JsonFormat(pattern = PATTERN)
    private LocalDateTime startTime;

    @JsonFormat(pattern = PATTERN)
    private LocalDateTime endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
