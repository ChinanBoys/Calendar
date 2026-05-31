package com.example.hello.llm;

import com.example.hello.config.VoiceCalProperties;
import com.example.hello.llm.dto.DeepSeekChatRequest;
import com.example.hello.llm.dto.DeepSeekChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * DeepSeek V4 HTTP 客户端（OpenAI 兼容 /chat/completions）。
 */
@Component
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final VoiceCalProperties properties;
    private final RestClient restClient;

    public DeepSeekClient(VoiceCalProperties properties) {
        this.properties = properties;
        int timeout = properties.getLlm().getTimeoutSeconds();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout * 1000);
        factory.setReadTimeout(timeout * 1000);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    /**
     * 调用 DeepSeek Chat Completions API。
     *
     * @param request 请求体
     * @return 助手回复正文；失败时抛出异常
     */
    public String chat(DeepSeekChatRequest request) {
        var llm = properties.getLlm();
        if (!llm.isConfigured()) {
            throw new IllegalStateException("DeepSeek API Key 未配置，请在 application-local.yml 中设置 voicecal.llm.api-key");
        }

        String url = trimTrailingSlash(llm.getBaseUrl()) + "/chat/completions";
        try {
            DeepSeekChatResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + llm.getApiKey())
                    .body(request)
                    .retrieve()
                    .body(DeepSeekChatResponse.class);

            if (response == null) {
                throw new RestClientException("DeepSeek 返回空响应");
            }
            if (response.getError() != null) {
                throw new RestClientException("DeepSeek API 错误: " + response.getError().getMessage());
            }
            if (response.getChoices() == null || response.getChoices().isEmpty()
                    || response.getChoices().get(0).getMessage() == null) {
                throw new RestClientException("DeepSeek 响应无 choices 内容");
            }
            DeepSeekChatResponse.ChatMessage message = response.getChoices().get(0).getMessage();
            String content = message.getContent();
            if (content == null || content.isBlank()) {
                // 部分 V4 模型在 thinking 模式下正文在 reasoning_content
                content = message.getReasoningContent();
            }
            if (content == null || content.isBlank()) {
                throw new RestClientException("DeepSeek 返回空 content");
            }
            return content.trim();
        } catch (RestClientException e) {
            log.error("DeepSeek API 调用失败: {}", e.getMessage());
            throw e;
        }
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null) {
            return "https://api.deepseek.com";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
