package com.example.hello.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DeepSeek Chat Completions 请求体（OpenAI 兼容格式）。
 */
public class DeepSeekChatRequest {

    private String model;
    private List<ChatMessage> messages;
    private Double temperature;
    private Boolean stream;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    /** V4 思考模式：解析任务关闭，避免 token 消耗在 reasoning 上 */
    private Thinking thinking;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    public Thinking getThinking() {
        return thinking;
    }

    public void setThinking(Thinking thinking) {
        this.thinking = thinking;
    }

    public static class Thinking {
        private String type;

        public Thinking() {
        }

        public Thinking(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage() {
        }

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class ResponseFormat {
        private String type;

        public ResponseFormat() {
        }

        public ResponseFormat(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
