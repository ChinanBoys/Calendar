package com.example.hello.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 业务配置，前缀 voicecal.*（见 application.yml）。
 */
@Component
@ConfigurationProperties(prefix = "voicecal")
public class VoiceCalProperties {

    /** 演示用默认用户 ID（未接入鉴权时写 voice_log 使用） */
    private Long defaultUserId = 1L;

    /** 默认时区（用户未传 tz 时使用） */
    private String defaultTimezone = "Asia/Shanghai";

    public Long getDefaultUserId() {
        return defaultUserId;
    }

    public void setDefaultUserId(Long defaultUserId) {
        this.defaultUserId = defaultUserId;
    }

    public String getDefaultTimezone() {
        return defaultTimezone;
    }

    public void setDefaultTimezone(String defaultTimezone) {
        this.defaultTimezone = defaultTimezone;
    }
}
