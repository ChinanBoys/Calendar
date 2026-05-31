package com.example.hello.controller;

import com.example.hello.common.Result;
import com.example.hello.dto.settings.SettingsVO;
import com.example.hello.dto.settings.UpdateSettingsRequest;
import com.example.hello.service.SettingsService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户设置模块。见 05-接口文档.md 第 4 节（4.1 / 4.2）。
 */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /** 4.1 查询设置 */
    @GetMapping
    public Result<SettingsVO> getSettings() {
        return Result.success(settingsService.getSettings());
    }

    /** 4.2 修改设置 */
    @PutMapping
    public Result<Void> updateSettings(@RequestBody UpdateSettingsRequest request) {
        settingsService.updateSettings(request);
        return Result.success();
    }
}
