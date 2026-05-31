package com.example.hello.config;

/**
 * DeepSeek 等大模型相关配置，前缀 voicecal.llm.*。
 * api-key 仅放在 application-local.yml（已 gitignore），勿写入 application.yml。
 */
public class LlmProperties {

    /** 是否启用 LLM 解析 */
    private boolean enabled = true;

    /** API 根地址 */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型 ID，如 deepseek-v4-pro / deepseek-v4-flash */
    private String model = "deepseek-v4-pro";

    /** API Key（本地 application-local.yml 注入） */
    private String apiKey;

    /** LLM 调用失败时是否回退规则解析器 */
    private boolean fallbackToRules = true;

    /** HTTP 超时（秒） */
    private int timeoutSeconds = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isFallbackToRules() {
        return fallbackToRules;
    }

    public void setFallbackToRules(boolean fallbackToRules) {
        this.fallbackToRules = fallbackToRules;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /** 是否具备调用 LLM 的条件 */
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
