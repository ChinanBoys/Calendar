package com.example.hello.controller;

import com.example.hello.common.Result;
import com.example.hello.service.SettingsService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 语音日志管理。见 05-接口文档.md 4.3 清除语音数据。
 */
@RestController
@RequestMapping("/api/voice-logs")
public class VoiceLogController {

    private final SettingsService settingsService;

    public VoiceLogController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /** 4.3 清除当前用户全部语音解析日志 */
    @DeleteMapping
    public Result<Void> clearVoiceLogs() {
        settingsService.clearVoiceLogs();
        return Result.success();
    }
}
