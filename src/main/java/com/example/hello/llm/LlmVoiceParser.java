package com.example.hello.llm;

import com.example.hello.dto.VoiceParseResult;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 使用大模型将自然语言解析为 {@link VoiceParseResult}。
 */
public interface LlmVoiceParser {

    /**
     * @param text    用户原文
     * @param baseNow 解析相对时间的基准时间
     * @param zone    时区
     * @return 结构化解析结果
     */
    VoiceParseResult parse(String text, LocalDateTime baseNow, ZoneId zone);
}
