package com.example.hello.entity;

import java.time.LocalDateTime;

/**
 * 语音解析日志表 voice_log 对应实体。
 * 见 02-数据库设计文档.md 3.4 节。
 */
public class VoiceLog {

    /** 主键 */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 识别/输入原文 */
    private String rawText;

    /** 解析意图：create/query/update/delete/unknown */
    private String intent;

    /** 结构化解析结果（JSON 字符串，对应 voice_log.parsed_json） */
    private String parsedJson;

    /** 创建时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getParsedJson() {
        return parsedJson;
    }

    public void setParsedJson(String parsedJson) {
        this.parsedJson = parsedJson;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
