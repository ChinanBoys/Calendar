package com.example.hello.controller;

import com.example.hello.common.Result;
import com.example.hello.dto.VoiceParseRequest;
import com.example.hello.dto.VoiceParseResult;
import com.example.hello.service.VoiceParseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 语音解析模块。
 * 见 05-接口文档.md 1. 语音解析模块。
 */
@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final VoiceParseService voiceParseService;

    public VoiceController(VoiceParseService voiceParseService) {
        this.voiceParseService = voiceParseService;
    }

    /**
     * 1.1 语音 / 文本解析。
     * 把语音转写后的自然语言文本解析为结构化 JSON，并写入 voice_log（不创建事件）。
     *
     * @param request text(必填) / now / tz
     * @return 结构化解析结果（intent + 相关字段 + 置信度）
     */
    @PostMapping("/parse")
    public Result<VoiceParseResult> parse(@Valid @RequestBody VoiceParseRequest request) {
        VoiceParseResult result = voiceParseService.parse(request);
        return Result.success(result);
    }
}
