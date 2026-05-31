package com.example.hello.dto.settings;

/**
 * GET /api/settings 响应 data（来自 user 表偏好字段）。
 */
public class SettingsVO {

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
