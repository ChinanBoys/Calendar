package com.example.hello.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * /api/voice/parse 请求体。
 * 见 05-接口文档.md 1.1.2 请求参数。
 */
public class VoiceParseRequest {

    /** 用户语音转写/输入的自然语言原文（必须） */
    @NotBlank(message = "text 不能为空")
    private String text;

    /** 客户端当前时间（ISO8601），缺省用服务端时间 */
    private String now;

    /** 时区，缺省取用户设置 timezone */
    private String tz;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNow() {
        return now;
    }

    public void setNow(String now) {
        this.now = now;
    }

    public String getTz() {
        return tz;
    }

    public void setTz(String tz) {
        this.tz = tz;
    }
}
