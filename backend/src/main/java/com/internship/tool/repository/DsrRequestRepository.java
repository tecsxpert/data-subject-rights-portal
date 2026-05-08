package com.internship.tool.repository;

import com.internship.tool.entity.DsrRequest;
import com.internship.tool.entity.DsrRequest.RequestType;
import com.internship.tool.entity.DsrRequest.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DsrRequestRepository extends JpaRepository<DsrRequest, Long> {

    // ── Basic finders ─────────────────────────────────────────

    Optional<DsrRequest> findByUuidAndDeletedAtIsNull(UUID uuid);

    Optional<DsrRequest> findByIdAndDeletedAtIsNull(Long id);

    // ── Paginated list (active only) ──────────────────────────

    Page<DsrRequest> findAllByDeletedAtIsNull(Pageable pageable);

    // ── Full-text + filter search ─────────────────────────────

    @Query("""
        SELECT r FROM DsrRequest r
        WHERE r.deletedAt IS NULL
          AND (CAST(:status AS text)   IS NULL OR r.status      = :status)
          AND (CAST(:type AS text)     IS NULL OR r.requestType = :type)
          AND (CAST(:priority AS text) IS NULL OR r.priority    = :priority)
          AND (CAST(:from AS timestamp) IS NULL OR r.createdAt  >= :from)
          AND (CAST(:to AS timestamp) IS NULL OR r.createdAt  <= :to)
          AND (
               CAST(:q AS text) IS NULL
            OR LOWER(r.subjectName)  LIKE LOWER(CONCAT('%', CAST(:q AS text), '%'))
            OR LOWER(r.subjectEmail) LIKE LOWER(CONCAT('%', CAST(:q AS text), '%'))
            OR LOWER(r.description)  LIKE LOWER(CONCAT('%', CAST(:q AS text), '%'))
          )
        ORDER BY r.createdAt DESC
        """)
    Page<DsrRequest> search(
            @Param("q")        String q,
            @Param("status")   Status status,
            @Param("type")     RequestType type,
            @Param("priority") DsrRequest.Priority priority,
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            Pageable pageable);

    // ── Stats queries ─────────────────────────────────────────

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(Status status);

    @Query("""
        SELECT COUNT(r) FROM DsrRequest r
        WHERE r.deletedAt IS NULL
          AND r.deadlineDate < CURRENT_DATE
          AND r.status NOT IN ('COMPLETED','REJECTED','CANCELLED')
        """)
    long countOverdue();

    @Query("""
        SELECT COUNT(r) FROM DsrRequest r
        WHERE r.deletedAt IS NULL
          AND r.createdAt >= :since
        """)
    long countSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT r.requestType AS type, COUNT(r) AS cnt
        FROM DsrRequest r
        WHERE r.deletedAt IS NULL
          AND r.createdAt >= :since
        GROUP BY r.requestType
        ORDER BY cnt DESC
        """)
    List<Object[]> countByTypeGrouped(@Param("since") LocalDateTime since);

    @Query("""
        SELECT r.status AS status, COUNT(r) AS cnt
        FROM DsrRequest r
        WHERE r.deletedAt IS NULL
        GROUP BY r.status
        """)
    List<Object[]> countByStatusGrouped();

    @Query(value = """
        SELECT COALESCE(AVG(
            EXTRACT(EPOCH FROM (resolved_at - created_at))/3600
        ), 0)
        FROM dsr_requests
        WHERE resolved_at IS NOT NULL AND deleted_at IS NULL
        """, nativeQuery = true)
    Double avgResolutionHours();

    // ── Deadline / scheduling ─────────────────────────────────

    @Query("""
        SELECT r FROM DsrRequest r
        WHERE r.deletedAt IS NULL
          AND r.status NOT IN ('COMPLETED','REJECTED','CANCELLED')
          AND r.deadlineDate BETWEEN :today AND :soon
        """)
    List<DsrRequest> findUpcomingDeadlines(
            @Param("today") LocalDate today,
            @Param("soon")  LocalDate soon);

    // ── Export ────────────────────────────────────────────────

    @Query("""
        SELECT r FROM DsrRequest r
        WHERE r.deletedAt IS NULL
          AND (CAST(:status AS text) IS NULL OR r.status = :status)
        ORDER BY r.createdAt DESC
        """)
    List<DsrRequest> findAllForExport(@Param("status") Status status);

    // ── Soft-delete bulk cleanup ──────────────────────────────

    @Modifying
    @Query("""
        UPDATE DsrRequest r
        SET r.deletedAt = :now
        WHERE r.id = :id AND r.deletedAt IS NULL
        """)
    int softDelete(@Param("id") Long id, @Param("now") LocalDateTime now);

    // ── Backdate createdAt for demo seeder (native query bypasses updatable=false) ──

    @Modifying
    @Transactional
    @Query(value = "UPDATE dsr_requests SET created_at = :createdAt WHERE id = :id",
           nativeQuery = true)
    void backdateCreatedAt(@Param("id") Long id, @Param("createdAt") LocalDateTime createdAt);
}
