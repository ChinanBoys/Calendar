package com.example.hello.service.impl;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.dto.VoiceParseRequest;
import com.example.hello.dto.VoiceParseResult;
import com.example.hello.entity.VoiceLog;
import com.example.hello.mapper.VoiceLogMapper;
import com.example.hello.service.NaturalLanguageParser;
import com.example.hello.service.VoiceParseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 语音解析服务实现：
 * 1) 确定基准时间（请求 now/tz 优先，否则服务端时间 + 默认时区）；
 * 2) 调用规则解析器得到结构化结果；
 * 3) 把本次解析写入 voice_log（仅落日志，不创建事件）。
 */
@Service
public class VoiceParseServiceImpl implements VoiceParseService {

    private static final Logger log = LoggerFactory.getLogger(VoiceParseServiceImpl.class);

    private final NaturalLanguageParser parser;
    private final VoiceLogMapper voiceLogMapper;
    private final VoiceCalProperties properties;
    private final ObjectMapper objectMapper;

    public VoiceParseServiceImpl(NaturalLanguageParser parser,
                                 VoiceLogMapper voiceLogMapper,
                                 VoiceCalProperties properties,
                                 ObjectMapper objectMapper) {
        this.parser = parser;
        this.voiceLogMapper = voiceLogMapper;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public VoiceParseResult parse(VoiceParseRequest request) {
        ZoneId zone = resolveZone(request.getTz());
        LocalDateTime baseNow = resolveNow(request.getNow(), zone);

        VoiceParseResult result = parser.parse(request.getText(), baseNow);

        saveVoiceLog(request.getText(), result);
        return result;
    }

    private ZoneId resolveZone(String tz) {
        String zoneId = (tz != null && !tz.isBlank()) ? tz : properties.getDefaultTimezone();
        try {
            return ZoneId.of(zoneId);
        } catch (Exception e) {
            log.warn("非法时区 [{}]，回退默认时区 {}", tz, properties.getDefaultTimezone());
            return ZoneId.of(properties.getDefaultTimezone());
        }
    }

    private LocalDateTime resolveNow(String now, ZoneId zone) {
        if (now != null && !now.isBlank()) {
            try {
                // 优先按带时区偏移的 ISO8601 解析，并换算到目标时区
                return OffsetDateTime.parse(now).atZoneSameInstant(zone).toLocalDateTime();
            } catch (DateTimeParseException ignore) {
                try {
                    return LocalDateTime.parse(now, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    log.warn("无法解析 now=[{}]，回退服务端当前时间", now);
                }
            }
        }
        return LocalDateTime.now(zone);
    }

    private void saveVoiceLog(String rawText, VoiceParseResult result) {
        try {
            VoiceLog voiceLog = new VoiceLog();
            voiceLog.setUserId(properties.getDefaultUserId());
            voiceLog.setRawText(rawText);
            voiceLog.setIntent(result.getIntent());
            voiceLog.setParsedJson(objectMapper.writeValueAsString(result));
            voiceLog.setCreateTime(LocalDateTime.now());
            voiceLogMapper.insert(voiceLog);
        } catch (Exception e) {
            // 落日志失败不应影响解析结果返回
            log.error("写入 voice_log 失败: {}", e.getMessage(), e);
        }
    }
}
