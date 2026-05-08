package com.internship.tool.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    /** POST /describe — returns {summary, generated_at, is_fallback} */
    public Map<String, Object> describe(String description, String requestType) {
        try {
            Map<String, String> body = Map.of(
                    "description",  description  != null ? description  : "",
                    "request_type", requestType  != null ? requestType  : "UNKNOWN"
            );
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    aiServiceUrl + "/describe", body, Map.class);
            return resp.getBody();
        } catch (Exception e) {
            log.warn("AI /describe unavailable: {}", e.getMessage());
            return fallbackDescribe();
        }
    }

    /** POST /recommend — returns list of {action_type, description, priority} */
    public JsonNode recommend(String description, String requestType) {
        try {
            Map<String, String> body = Map.of(
                    "description",  description  != null ? description  : "",
                    "request_type", requestType  != null ? requestType  : "UNKNOWN"
            );
            ResponseEntity<JsonNode> resp = restTemplate.postForEntity(
                    aiServiceUrl + "/recommend", body, JsonNode.class);
            return resp.getBody();
        } catch (Exception e) {
            log.warn("AI /recommend unavailable: {}", e.getMessage());
            return null;
        }
    }

    /** POST /generate-report — returns structured report JSON */
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateReport(Long dsrId, String description, String requestType) {
        try {
            Map<String, Object> body = Map.of(
                    "dsr_id",       dsrId,
                    "description",  description  != null ? description  : "",
                    "request_type", requestType  != null ? requestType  : "UNKNOWN"
            );
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    aiServiceUrl + "/generate-report", body, Map.class);
            return resp.getBody();
        } catch (Exception e) {
            log.warn("AI /generate-report unavailable: {}", e.getMessage());
            return Map.of(
                    "title",       "Report unavailable",
                    "summary",     "The AI service is currently unavailable.",
                    "is_fallback", true
            );
        }
    }

    private Map<String, Object> fallbackDescribe() {
        return Map.of(
                "summary",       "AI service unavailable — please review manually.",
                "generated_at",  java.time.Instant.now().toString(),
                "is_fallback",   true
        );
    }
}
