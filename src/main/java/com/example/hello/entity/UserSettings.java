package com.example.hello.entity;

/**
 * user 表偏好设置字段（不含账号信息）。
 */
public class UserSettings {

    private String timezone;
    private Integer defaultReminderMinutes;
    private Boolean notifyEnabled;
    private String voiceRetention;

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getDefaultReminderMinutes() {
        return defaultReminderMinutes;
    }

    public void setDefaultReminderMinutes(Integer defaultReminderMinutes) {
        this.defaultReminderMinutes = defaultReminderMinutes;
    }

    public Boolean getNotifyEnabled() {
        return notifyEnabled;
    }

    public void setNotifyEnabled(Boolean notifyEnabled) {
        this.notifyEnabled = notifyEnabled;
    }

    public String getVoiceRetention() {
        return voiceRetention;
    }

    public void setVoiceRetention(String voiceRetention) {
        this.voiceRetention = voiceRetention;
    }
}
