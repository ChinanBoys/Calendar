package com.example.hello.dto.event;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * POST /api/events 请求体。
 */
public class CreateEventRequest {

    @NotBlank(message = "title 不能为空")
    private String title;

    @NotBlank(message = "startTime 不能为空")
    private String startTime;

    @NotBlank(message = "endTime 不能为空")
    private String endTime;

    private Boolean allDay;
    private String location;
    private String note;
    private String recurrence;
    private List<Integer> reminderOffsets;
    private Boolean force;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public List<Integer> getReminderOffsets() {
        return reminderOffsets;
    }

    public void setReminderOffsets(List<Integer> reminderOffsets) {
        this.reminderOffsets = reminderOffsets;
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }
}
