package com.example.hello.service.impl;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.dto.reminder.UpcomingReminderVO;
import com.example.hello.mapper.ReminderMapper;
import com.example.hello.service.ReminderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    private static final int DEFAULT_HOURS = 24;
    private static final int MAX_HOURS = 24 * 30;

    private final ReminderMapper reminderMapper;
    private final VoiceCalProperties properties;

    public ReminderServiceImpl(ReminderMapper reminderMapper, VoiceCalProperties properties) {
        this.reminderMapper = reminderMapper;
        this.properties = properties;
    }

    @Override
    public List<UpcomingReminderVO> listUpcoming(int hours) {
        int safeHours = hours > 0 ? hours : DEFAULT_HOURS;
        if (safeHours > MAX_HOURS) {
            safeHours = MAX_HOURS;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(safeHours);

        return reminderMapper.selectUpcoming(properties.getDefaultUserId(), now, end);
    }
}
