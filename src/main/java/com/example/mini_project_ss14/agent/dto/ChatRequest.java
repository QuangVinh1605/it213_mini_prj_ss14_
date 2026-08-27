package com.example.mini_project_ss14.agent.dto;

public class ChatRequest {
    private String conversationId = "default-user"; // Mặc định chung 1 session nếu không truyền
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String conversationId, String message) {
        this.conversationId = conversationId;
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
