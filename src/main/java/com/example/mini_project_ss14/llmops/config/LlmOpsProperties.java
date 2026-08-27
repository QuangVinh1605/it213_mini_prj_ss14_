package com.example.mini_project_ss14.llmops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "llmops")
public class LlmOpsProperties {

    private boolean enabled = true;
    private String provider = "openai";
    private String model = "gpt-4o-mini";
    private boolean capturePayloads = true;
    private int maxPayloadChars = 4000;
    private Guard guard = new Guard();
    private Token token = new Token();
    private Cost cost = new Cost();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isCapturePayloads() {
        return capturePayloads;
    }

    public void setCapturePayloads(boolean capturePayloads) {
        this.capturePayloads = capturePayloads;
    }

    public int getMaxPayloadChars() {
        return maxPayloadChars;
    }

    public void setMaxPayloadChars(int maxPayloadChars) {
        this.maxPayloadChars = maxPayloadChars;
    }

    public Guard getGuard() {
        return guard;
    }

    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    public static class Guard {
        private boolean enabled = true;
        private int maxCallsPerWindow = 8;
        private long windowSeconds = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxCallsPerWindow() {
            return maxCallsPerWindow;
        }

        public void setMaxCallsPerWindow(int maxCallsPerWindow) {
            this.maxCallsPerWindow = maxCallsPerWindow;
        }

        public long getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(long windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }

    public static class Token {
        private int estimatedCharsPerToken = 4;

        public int getEstimatedCharsPerToken() {
            return estimatedCharsPerToken;
        }

        public void setEstimatedCharsPerToken(int estimatedCharsPerToken) {
            this.estimatedCharsPerToken = estimatedCharsPerToken;
        }
    }

    public static class Cost {
        private double inputUsdPer1kTokens = 0.00015;
        private double outputUsdPer1kTokens = 0.0006;

        public double getInputUsdPer1kTokens() {
            return inputUsdPer1kTokens;
        }

        public void setInputUsdPer1kTokens(double inputUsdPer1kTokens) {
            this.inputUsdPer1kTokens = inputUsdPer1kTokens;
        }

        public double getOutputUsdPer1kTokens() {
            return outputUsdPer1kTokens;
        }

        public void setOutputUsdPer1kTokens(double outputUsdPer1kTokens) {
            this.outputUsdPer1kTokens = outputUsdPer1kTokens;
        }
    }
}
