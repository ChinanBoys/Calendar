package com.example.hello.service.impl;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.dto.reminder.UpcomingReminderVO;
import com.example.hello.entity.UserSettings;
import com.example.hello.mapper.ReminderMapper;
import com.example.hello.mapper.UserMapper;
import com.example.hello.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderMapper reminderMapper;
    private final UserMapper userMapper;
    private final VoiceCalProperties properties;
    private final Clock clock;

    @Autowired
    public ReminderServiceImpl(ReminderMapper reminderMapper,
                               UserMapper userMapper,
                               VoiceCalProperties properties) {
        this(reminderMapper, userMapper, properties, Clock.systemUTC());
    }

    ReminderServiceImpl(ReminderMapper reminderMapper,
                        UserMapper userMapper,
                        VoiceCalProperties properties,
                        Clock clock) {
        this.reminderMapper = reminderMapper;
        this.userMapper = userMapper;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public List<UpcomingReminderVO> listUpcoming(int hours) {
        Long userId = properties.getDefaultUserId();
        LocalDateTime now = LocalDateTime.now(clock.withZone(resolveUserZone(userId)));

        return reminderMapper.selectUpcoming(userId, now);
    }

    @Override
    public void markRead(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("reminder id 不能为空");
        }
        int updated = reminderMapper.markRead(id, properties.getDefaultUserId());
        if (updated == 0) {
            throw new IllegalArgumentException("提醒不存在");
        }
    }

    private ZoneId resolveUserZone(Long userId) {
        UserSettings settings = userMapper.selectSettingsByUserId(userId);
        String zone = settings != null && settings.getTimezone() != null && !settings.getTimezone().isBlank()
                ? settings.getTimezone()
                : properties.getDefaultTimezone();

        if (zone == null || zone.isBlank()) {
            return ZoneId.of("Asia/Shanghai");
        }
        try {
            return ZoneId.of(zone);
        } catch (DateTimeException e) {
            return ZoneId.of("Asia/Shanghai");
        }
    }
}
