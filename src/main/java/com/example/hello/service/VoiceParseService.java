package com.example.hello.service;

import com.example.hello.dto.VoiceParseRequest;
import com.example.hello.dto.VoiceParseResult;

/**
 * 语音/文本解析服务。
 */
public interface VoiceParseService {

    /**
     * 解析自然语言文本为结构化结果，并把本次解析落入 voice_log。
     *
     * @param request 解析请求（text 必填，now/tz 可选）
     * @return 结构化解析结果
     */
    VoiceParseResult parse(VoiceParseRequest request);
}
