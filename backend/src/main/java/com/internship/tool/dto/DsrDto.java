package com.internship.tool.dto;

import com.internship.tool.entity.DsrRequest.Priority;
import com.internship.tool.entity.DsrRequest.RequestType;
import com.internship.tool.entity.DsrRequest.Status;
import jakarta.validation.constraints.*;
import lombok.*;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// ── Request DTOs ─────────────────────────────────────────────

public class DsrDto {

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "Subject name is required")
        @Size(max = 255, message = "Subject name too long")
        private String subjectName;

        @NotBlank(message = "Subject email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255)
        private String subjectEmail;

        @NotNull(message = "Request type is required")
        private RequestType requestType;

        @Size(max = 5000, message = "Description too long")
        private String description;

        private Priority priority = Priority.MEDIUM;

        private LocalDate deadlineDate;

        private Long assignedToId;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {

        @Size(max = 255)
        private String subjectName;

        @Email
        @Size(max = 255)
        private String subjectEmail;

        private RequestType requestType;

        private Status status;

        @Size(max = 5000)
        private String description;

        private Priority priority;

        private LocalDate deadlineDate;

        private Long assignedToId;
    }

    // ── Response DTOs ─────────────────────────────────────────

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private UUID uuid;
        private String subjectName;
        private String subjectEmail;
        private RequestType requestType;
        private Status status;
        private String description;
        private String aiDescription;
        private JsonNode aiRecommendations;
        private JsonNode aiReport;
        private Boolean isAiFallback;
        private Priority priority;
        private String assignedToName;
        private LocalDate deadlineDate;
        private LocalDateTime resolvedAt;
        private String fileOriginalName;
        private Boolean hasFile;
        private Boolean isOverdue;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class StatsResponse {
        private long total;
        private long pending;
        private long inProgress;
        private long completed;
        private long rejected;
        private long overdue;
        private long newThisWeek;
        private long newThisMonth;
        private Double avgResolutionHours;
        private Map<String, Long> byType;
        private Map<String, Long> byStatus;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class SearchParams {
        private String q;
        private Status status;
        private RequestType requestType;
        private Priority priority;
        private String from;
        private String to;
        private int page = 0;
        private int size = 20;
        private String sort = "createdAt";
        private String dir  = "desc";
    }
}
