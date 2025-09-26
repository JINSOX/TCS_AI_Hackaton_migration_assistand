package com.migration_assistand.controller;

import com.migration_assistand.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
public class LLMController {

    private final LLMService llmService;

    @Autowired
    public LLMController(LLMService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/ask")
    public ResponseEntity<LLMResponse> askLLM(@RequestBody LLMRequest request) {
        try {
            String answer = llmService.ask(request.getProjectPath(), request.getQuestion());
            return ResponseEntity.ok(new LLMResponse(answer));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new LLMResponse("Error: " + e.getMessage()));
        }
    }

    // DTOs
    public static class LLMRequest {
        private String projectPath;
        private String question;
        public String getProjectPath() { return projectPath; }
        public void setProjectPath(String projectPath) { this.projectPath = projectPath; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }

    public static class LLMResponse {
        private String answer;
        public LLMResponse(String answer) { this.answer = answer; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}