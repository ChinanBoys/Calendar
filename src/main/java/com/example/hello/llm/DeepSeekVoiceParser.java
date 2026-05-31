package com.example.hello.llm;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.dto.VoiceParseResult;
import com.example.hello.llm.dto.DeepSeekChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用 DeepSeek V4 将日历相关自然语言解析为结构化 JSON。
 */
@Component
public class DeepSeekVoiceParser implements LlmVoiceParser {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekVoiceParser.class);

    private static final Set<String> VALID_INTENTS =
            Set.of("create", "query", "update", "delete", "unknown");

    private static final Pattern JSON_BLOCK =
            Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private final DeepSeekClient deepSeekClient;
    private final VoiceCalProperties properties;
    private final ObjectMapper objectMapper;

    public DeepSeekVoiceParser(DeepSeekClient deepSeekClient,
                               VoiceCalProperties properties,
                               ObjectMapper objectMapper) {
        this.deepSeekClient = deepSeekClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public VoiceParseResult parse(String text, LocalDateTime baseNow, ZoneId zone) {
        String systemPrompt = buildSystemPrompt(baseNow, zone);
        DeepSeekChatRequest request = new DeepSeekChatRequest();
        request.setModel(properties.getLlm().getModel());
        request.setTemperature(0.1);
        request.setStream(false);
        request.setMaxTokens(2048);
        request.setThinking(new DeepSeekChatRequest.Thinking("disabled"));
        request.setResponseFormat(new DeepSeekChatRequest.ResponseFormat("json_object"));
        request.setMessages(List.of(
                new DeepSeekChatRequest.ChatMessage("system", systemPrompt),
                new DeepSeekChatRequest.ChatMessage("user", text)
        ));

        String content = deepSeekClient.chat(request);
        String json = extractJson(content);
        try {
            VoiceParseResult result = objectMapper.readValue(json, VoiceParseResult.class);
            normalizeResult(result, text);
            return result;
        } catch (Exception e) {
            log.warn("DeepSeek 返回 JSON 解析失败，原始内容: {}", content);
            throw new IllegalStateException("DeepSeek 返回内容无法解析为 VoiceParseResult: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(LocalDateTime baseNow, ZoneId zone) {
        String nowStr = baseNow.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return """
                你是语音日历 VoiceCal 的语义解析引擎。把用户的中文自然语言解析为 JSON，用于前端「解析结果确认卡」。
                
                【当前上下文】
                - 当前时间（基准）: %s
                - 时区: %s
                请把「今天/明天/后天/下周X」等相对时间换算为绝对时间，格式 yyyy-MM-dd'T'HH:mm:ss（24小时制，不要带时区后缀）。
                
                【输出要求】
                只输出一个 JSON 对象，不要 markdown、不要解释。字段名使用 camelCase，与下列 schema 一致；未提及的字段不要输出（或设为 null）。
                
                {
                  "intent": "create|query|update|delete|unknown",
                  "title": "string，intent=create 时事件标题",
                  "startTime": "string，intent=create/update 时开始时间",
                  "endTime": "string，intent=create 时结束时间",
                  "allDay": "boolean，是否全天",
                  "location": "string|null，地点；未提及必须为 null",
                  "reminderOffsets": [15],
                  "recurrence": "none|daily|weekly|monthly",
                  "queryStart": "string，intent=query 时查询范围开始",
                  "queryEnd": "string，intent=query 时查询范围结束",
                  "matchKeyword": "string，intent=update/delete 时用于定位已有事件的关键词",
                  "confidence": { "intent": 0.99, "title": 0.9, ... },
                  "rawText": "用户原文"
                }
                
                【意图规则】
                - create: 添加/安排/预约/提醒我做某事，含具体时间或事件类型
                - query: 询问有什么安排、查日程、几点有什么
                - update: 修改、改到、推迟、提前到
                - delete: 删除、取消、删掉
                - unknown: 无法判断
                
                【示例】
                用户: 我明天下午三点到五点有个会议，地点在3号会议室，提前15分钟提醒
                输出 intent=create, title=会议, startTime 明天15:00, endTime 明天17:00, location=3号会议室, reminderOffsets=[15], recurrence=none
                
                用户: 我明天有什么安排
                输出 intent=query, queryStart/queryEnd 为明天 00:00:00 到 23:59:59
                """.formatted(nowStr, zone.getId());
    }

    private String extractJson(String content) {
        Matcher matcher = JSON_BLOCK.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private void normalizeResult(VoiceParseResult result, String rawText) {
        result.setRawText(rawText);
        if (result.getIntent() == null || !VALID_INTENTS.contains(result.getIntent())) {
            result.setIntent("unknown");
        }
        if (result.getRecurrence() == null || result.getRecurrence().isBlank()) {
            result.setRecurrence("none");
        }
        // 若模型把 confidence 写成嵌套对象但类型不匹配，Jackson 已处理；补默认 intent 置信度
        if (result.getConfidence() == null || result.getConfidence().isEmpty()) {
            result.setConfidence(java.util.Map.of("intent", 0.85));
        }
    }
}
