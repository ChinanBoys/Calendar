package com.example.hello.service;

import com.example.hello.dto.settings.SettingsVO;
import com.example.hello.dto.settings.UpdateSettingsRequest;

/**
 * 用户设置模块服务。
 */
public interface SettingsService {

    SettingsVO getSettings();

    void updateSettings(UpdateSettingsRequest request);

    void clearVoiceLogs();
}
