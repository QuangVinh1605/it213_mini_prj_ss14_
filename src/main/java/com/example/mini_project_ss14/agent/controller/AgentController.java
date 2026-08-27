package com.example.mini_project_ss14.agent.controller;

import com.example.mini_project_ss14.agent.dto.ChatRequest;
import com.example.mini_project_ss14.agent.dto.ChatResponse;
import com.example.mini_project_ss14.agent.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/operations")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Vui lòng nhập nội dung sự cố."));
        }
        
        try {
            String response = agentService.processChat(request.getMessage());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ChatResponse("Hệ thống đang bận hoặc có lỗi xảy ra, vui lòng thử lại sau. Lỗi: " + e.getMessage()));
        }
    }
}
