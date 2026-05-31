package com.example.hello.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * /api/voice/parse 响应中的 data（解析结果）。
 * 见 05-接口文档.md 1.1.3 响应数据。
 * null 字段不参与序列化（已在 application.yml 中配置 non_null）。
 */
public class VoiceParseResult {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    /** 意图：create/query/update/delete/unknown */
    private String intent;

    /** 标题（intent=create 时） */
    private String title;

    /** 开始时间（已换算为绝对时间） */
    @JsonFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime endTime;

    /** 是否全天 */
    private Boolean allDay;

    /** 地点（null 表示未提及，前端高亮提示核对） */
    private String location;

    /** 提醒提前分钟数列表 */
    private List<Integer> reminderOffsets;

    /** 重复规则 none/daily/weekly/monthly */
    private String recurrence;

    /** 查询起始时间（intent=query 时） */
    @JsonFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime queryStart;

    /** 查询结束时间（intent=query 时） */
    @JsonFormat(pattern = DATETIME_PATTERN)
    private LocalDateTime queryEnd;

    /** 定位事件的关键词（intent=update/delete 时） */
    private String matchKeyword;

    /** 各字段置信度（0~1），前端据此高亮低置信度字段 */
    private Map<String, Double> confidence;

    /** 识别原文 */
    private String rawText;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
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

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Integer> getReminderOffsets() {
        return reminderOffsets;
    }

    public void setReminderOffsets(List<Integer> reminderOffsets) {
        this.reminderOffsets = reminderOffsets;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public LocalDateTime getQueryStart() {
        return queryStart;
    }

    public void setQueryStart(LocalDateTime queryStart) {
        this.queryStart = queryStart;
    }

    public LocalDateTime getQueryEnd() {
        return queryEnd;
    }

    public void setQueryEnd(LocalDateTime queryEnd) {
        this.queryEnd = queryEnd;
    }

    public String getMatchKeyword() {
        return matchKeyword;
    }

    public void setMatchKeyword(String matchKeyword) {
        this.matchKeyword = matchKeyword;
    }

    public Map<String, Double> getConfidence() {
        return confidence;
    }

    public void setConfidence(Map<String, Double> confidence) {
        this.confidence = confidence;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
