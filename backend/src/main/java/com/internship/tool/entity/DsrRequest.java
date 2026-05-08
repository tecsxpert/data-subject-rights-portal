package com.internship.tool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "dsr_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DsrRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "subject_email", nullable = false)
    private String subjectEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_recommendations", columnDefinition = "jsonb")
    private JsonNode aiRecommendations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_report", columnDefinition = "jsonb")
    private JsonNode aiReport;

    @Column(name = "is_ai_fallback")
    @Builder.Default
    private Boolean isAiFallback = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_original_name")
    private String fileOriginalName;

    @Column(name = "file_mime_type")
    private String fileMimeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ── Enums ─────────────────────────────────────────────────

    public enum RequestType {
        ACCESS, ERASURE, RECTIFICATION, PORTABILITY, RESTRICTION, OBJECTION
    }

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, REJECTED, CANCELLED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // ── Helpers ───────────────────────────────────────────────

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        return deadlineDate != null
                && LocalDate.now().isAfter(deadlineDate)
                && status != Status.COMPLETED
                && status != Status.REJECTED
                && status != Status.CANCELLED;
    }
}
