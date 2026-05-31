package com.example.hello.service.impl;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.dto.settings.SettingsVO;
import com.example.hello.dto.settings.UpdateSettingsRequest;
import com.example.hello.entity.UserSettings;
import com.example.hello.mapper.UserMapper;
import com.example.hello.mapper.VoiceLogMapper;
import com.example.hello.service.SettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class SettingsServiceImpl implements SettingsService {

    private static final Set<String> VALID_VOICE_RETENTION = Set.of("none", "7d", "forever");

    private final UserMapper userMapper;
    private final VoiceLogMapper voiceLogMapper;
    private final VoiceCalProperties properties;

    public SettingsServiceImpl(UserMapper userMapper,
                               VoiceLogMapper voiceLogMapper,
                               VoiceCalProperties properties) {
        this.userMapper = userMapper;
        this.voiceLogMapper = voiceLogMapper;
        this.properties = properties;
    }

    @Override
    public SettingsVO getSettings() {
        UserSettings settings = requireSettings();
        return toVO(settings);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSettings(UpdateSettingsRequest request) {
        requireSettings();

        if (request.getVoiceRetention() != null
                && !VALID_VOICE_RETENTION.contains(request.getVoiceRetention())) {
            throw new IllegalArgumentException("voiceRetention 必须为 none、7d 或 forever");
        }
        if (request.getDefaultReminderMinutes() != null && request.getDefaultReminderMinutes() <= 0) {
            throw new IllegalArgumentException("defaultReminderMinutes 必须大于 0");
        }

        UserSettings patch = new UserSettings();
        patch.setTimezone(request.getTimezone());
        patch.setDefaultReminderMinutes(request.getDefaultReminderMinutes());
        patch.setNotifyEnabled(request.getNotifyEnabled());
        patch.setVoiceRetention(request.getVoiceRetention());

        int rows = userMapper.updateSettingsSelective(currentUserId(), patch);
        if (rows == 0) {
            throw new SettingsNotFoundException("用户不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearVoiceLogs() {
        voiceLogMapper.deleteByUserId(currentUserId());
    }

    private UserSettings requireSettings() {
        UserSettings settings = userMapper.selectSettingsByUserId(currentUserId());
        if (settings == null) {
            throw new SettingsNotFoundException("用户不存在");
        }
        return settings;
    }

    private Long currentUserId() {
        return properties.getDefaultUserId();
    }

    private SettingsVO toVO(UserSettings settings) {
        SettingsVO vo = new SettingsVO();
        vo.setTimezone(settings.getTimezone());
        vo.setDefaultReminderMinutes(settings.getDefaultReminderMinutes());
        vo.setNotifyEnabled(settings.getNotifyEnabled());
        vo.setVoiceRetention(settings.getVoiceRetention());
        return vo;
    }

    /** 用户不存在 */
    public static class SettingsNotFoundException extends RuntimeException {
        public SettingsNotFoundException(String message) {
            super(message);
        }
    }
}
