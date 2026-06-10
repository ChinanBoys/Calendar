package com.example.hello.service.impl;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.entity.UserSettings;
import com.example.hello.mapper.ReminderMapper;
import com.example.hello.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReminderServiceImplTest {

    @Test
    void listUpcomingUsesConfiguredTimezoneForQueryWindow() {
        TimeZone originalZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            ReminderMapper reminderMapper = mock(ReminderMapper.class);
            UserMapper userMapper = mock(UserMapper.class);
            VoiceCalProperties properties = new VoiceCalProperties();
            properties.setDefaultUserId(1L);
            properties.setDefaultTimezone("Asia/Shanghai");
            UserSettings settings = new UserSettings();
            settings.setTimezone("Asia/Shanghai");
            Clock fixedClock = Clock.fixed(
                    Instant.parse("2026-06-10T07:28:00Z"),
                    ZoneOffset.UTC);

            when(userMapper.selectSettingsByUserId(1L)).thenReturn(settings);
            when(reminderMapper.selectUpcoming(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of());

            ReminderServiceImpl service =
                    new ReminderServiceImpl(reminderMapper, userMapper, properties, fixedClock);
            service.listUpcoming(24);

            ArgumentCaptor<LocalDateTime> fromTime = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<LocalDateTime> toTime = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(reminderMapper).selectUpcoming(eq(1L), fromTime.capture(), toTime.capture());

            assertEquals(LocalDateTime.of(2026, 6, 10, 15, 28), fromTime.getValue());
            assertEquals(LocalDateTime.of(2026, 6, 11, 15, 28), toTime.getValue());
        } finally {
            TimeZone.setDefault(originalZone);
        }
    }
}
