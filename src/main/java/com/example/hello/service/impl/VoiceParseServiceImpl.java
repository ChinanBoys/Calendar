package com.example.hello.service.impl;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.dto.VoiceParseRequest;
import com.example.hello.dto.VoiceParseResult;
import com.example.hello.entity.VoiceLog;
import com.example.hello.llm.LlmVoiceParser;
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
 * 2) 优先调用 DeepSeek V4 大模型解析，失败且开启回退时使用规则解析器；
 * 3) 把本次解析写入 voice_log（仅落日志，不创建事件）。
 */
@Service
public class VoiceParseServiceImpl implements VoiceParseService {

    private static final Logger log = LoggerFactory.getLogger(VoiceParseServiceImpl.class);

    private final LlmVoiceParser llmVoiceParser;
    private final NaturalLanguageParser ruleParser;
    private final VoiceLogMapper voiceLogMapper;
    private final VoiceCalProperties properties;
    private final ObjectMapper objectMapper;

    public VoiceParseServiceImpl(LlmVoiceParser llmVoiceParser,
                                 NaturalLanguageParser ruleParser,
                                 VoiceLogMapper voiceLogMapper,
                                 VoiceCalProperties properties,
                                 ObjectMapper objectMapper) {
        this.llmVoiceParser = llmVoiceParser;
        this.ruleParser = ruleParser;
        this.voiceLogMapper = voiceLogMapper;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public VoiceParseResult parse(VoiceParseRequest request) {
        ZoneId zone = resolveZone(request.getTz());
        LocalDateTime baseNow = resolveNow(request.getNow(), zone);

        VoiceParseResult result = parseWithLlmOrRules(request.getText(), baseNow, zone);

        saveVoiceLog(request.getText(), result);
        return result;
    }

    private VoiceParseResult parseWithLlmOrRules(String text, LocalDateTime baseNow, ZoneId zone) {
        var llm = properties.getLlm();
        if (llm.isConfigured()) {
            try {
                log.debug("使用 DeepSeek 模型 [{}] 解析语音文本", llm.getModel());
                return llmVoiceParser.parse(text, baseNow, zone);
            } catch (Exception e) {
                log.warn("DeepSeek 解析失败: {}", e.getMessage());
                if (!llm.isFallbackToRules()) {
                    throw new IllegalStateException("DeepSeek 解析失败且未开启规则回退: " + e.getMessage(), e);
                }
                log.info("回退到规则解析器");
            }
        } else {
            log.debug("未配置 DeepSeek API Key，使用规则解析器");
        }
        return ruleParser.parse(text, baseNow);
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
            log.error("写入 voice_log 失败: {}", e.getMessage(), e);
        }
    }
}
