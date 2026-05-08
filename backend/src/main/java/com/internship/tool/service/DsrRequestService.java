package com.internship.tool.service;

import com.internship.tool.dto.DsrDto;
import com.internship.tool.entity.DsrRequest;
import com.internship.tool.entity.DsrRequest.*;
import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.DsrRequestRepository;
import com.internship.tool.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DsrRequestService {

    private final DsrRequestRepository dsrRepo;
    private final UserRepository       userRepo;
    private final AuditLogService      auditService;
    private final AiServiceClient      aiClient;

    private static final String CACHE_LIST   = "dsr-list";
    private static final String CACHE_SINGLE = "dsr-single";
    private static final String CACHE_STATS  = "dsr-stats";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_DATE_TIME;

    // ── CREATE ────────────────────────────────────────────────

    @Transactional
    @CacheEvict(cacheNames = {CACHE_LIST, CACHE_STATS}, allEntries = true)
    public DsrDto.Response create(DsrDto.CreateRequest req, Long currentUserId) {
        User creator = userRepo.findByIdAndActiveTrue(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        DsrRequest entity = DsrRequest.builder()
                .subjectName(req.getSubjectName().strip())
                .subjectEmail(req.getSubjectEmail().strip().toLowerCase())
                .requestType(req.getRequestType())
                .description(req.getDescription())
                .priority(req.getPriority() != null ? req.getPriority() : Priority.MEDIUM)
                .deadlineDate(req.getDeadlineDate())
                .createdBy(creator)
                .build();

        if (req.getAssignedToId() != null) {
            entity.setAssignedTo(userRepo.findByIdAndActiveTrue(req.getAssignedToId())
                    .orElse(null));
        }

        DsrRequest saved = dsrRepo.save(entity);
        auditService.log("DSR_REQUEST", saved.getId(), "CREATE", currentUserId, null, toMap(saved));

        // Trigger AI enrichment asynchronously — non-blocking
        enrichWithAiAsync(saved.getId(), saved.getDescription(), saved.getRequestType().name());

        return toResponse(saved);
    }

    // ── READ (single) ─────────────────────────────────────────

    @Cacheable(value = CACHE_SINGLE, key = "#id")
    public DsrDto.Response getById(Long id) {
        return toResponse(findActiveOrThrow(id));
    }

    // ── READ (paginated list) ─────────────────────────────────

    @Cacheable(value = CACHE_LIST, key = "{#params.page,#params.size,#params.sort,#params.dir}")
    public Page<DsrDto.Response> getAll(DsrDto.SearchParams params) {
        Pageable pageable = buildPageable(params);
        return dsrRepo.findAllByDeletedAtIsNull(pageable).map(this::toResponse);
    }

    // ── SEARCH ────────────────────────────────────────────────

    public Page<DsrDto.Response> search(DsrDto.SearchParams params) {
        LocalDateTime from = params.getFrom() != null
                ? LocalDateTime.parse(params.getFrom(), DTF) : null;
        LocalDateTime to   = params.getTo()   != null
                ? LocalDateTime.parse(params.getTo(), DTF)   : null;

        Page<DsrRequest> page = dsrRepo.search(
                params.getQ(), params.getStatus(), params.getRequestType(),
                params.getPriority(), from, to, buildPageable(params));

        return page.map(this::toResponse);
    }

    // ── UPDATE ────────────────────────────────────────────────

    @Transactional
    @CacheEvict(cacheNames = {CACHE_LIST, CACHE_SINGLE, CACHE_STATS}, allEntries = true)
    public DsrDto.Response update(Long id, DsrDto.UpdateRequest req, Long currentUserId) {
        DsrRequest entity = findActiveOrThrow(id);
        Map<String, Object> before = toMap(entity);

        if (req.getSubjectName()  != null) entity.setSubjectName(req.getSubjectName().strip());
        if (req.getSubjectEmail() != null) entity.setSubjectEmail(req.getSubjectEmail().strip().toLowerCase());
        if (req.getRequestType()  != null) entity.setRequestType(req.getRequestType());
        if (req.getPriority()     != null) entity.setPriority(req.getPriority());
        if (req.getDeadlineDate() != null) entity.setDeadlineDate(req.getDeadlineDate());
        if (req.getDescription()  != null) entity.setDescription(req.getDescription());

        if (req.getStatus() != null) {
            entity.setStatus(req.getStatus());
            if (req.getStatus() == Status.COMPLETED || req.getStatus() == Status.REJECTED) {
                entity.setResolvedAt(LocalDateTime.now());
            }
        }
        if (req.getAssignedToId() != null) {
            entity.setAssignedTo(userRepo.findByIdAndActiveTrue(req.getAssignedToId()).orElse(null));
        }

        DsrRequest saved = dsrRepo.save(entity);
        auditService.log("DSR_REQUEST", saved.getId(), "UPDATE", currentUserId, before, toMap(saved));
        return toResponse(saved);
    }

    // ── SOFT DELETE ───────────────────────────────────────────

    @Transactional
    @CacheEvict(cacheNames = {CACHE_LIST, CACHE_SINGLE, CACHE_STATS}, allEntries = true)
    public void delete(Long id, Long currentUserId) {
        DsrRequest entity = findActiveOrThrow(id);
        int rows = dsrRepo.softDelete(id, LocalDateTime.now());
        if (rows == 0) throw new ResourceNotFoundException("DsrRequest", id);
        auditService.log("DSR_REQUEST", id, "DELETE", currentUserId, toMap(entity), null);
    }

    // ── STATS ─────────────────────────────────────────────────

    @Cacheable(value = CACHE_STATS)
    public DsrDto.StatsResponse getStats() {
        LocalDateTime weekAgo  = LocalDateTime.now().minusDays(7);
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        Map<String, Long> byType = dsrRepo.countByTypeGrouped(monthAgo).stream()
                .collect(Collectors.toMap(r -> r[0].toString(), r -> (Long) r[1]));

        Map<String, Long> byStatus = dsrRepo.countByStatusGrouped().stream()
                .collect(Collectors.toMap(r -> r[0].toString(), r -> (Long) r[1]));

        return DsrDto.StatsResponse.builder()
                .total(dsrRepo.countByDeletedAtIsNull())
                .pending(dsrRepo.countByStatusAndDeletedAtIsNull(Status.PENDING))
                .inProgress(dsrRepo.countByStatusAndDeletedAtIsNull(Status.IN_PROGRESS))
                .completed(dsrRepo.countByStatusAndDeletedAtIsNull(Status.COMPLETED))
                .rejected(dsrRepo.countByStatusAndDeletedAtIsNull(Status.REJECTED))
                .overdue(dsrRepo.countOverdue())
                .newThisWeek(dsrRepo.countSince(weekAgo))
                .newThisMonth(dsrRepo.countSince(monthAgo))
                .avgResolutionHours(dsrRepo.avgResolutionHours())
                .byType(byType)
                .byStatus(byStatus)
                .build();
    }

    // ── FILE UPLOAD ───────────────────────────────────────────

    @Transactional
    @CacheEvict(cacheNames = {CACHE_SINGLE}, key = "#id")
    public DsrDto.Response attachFile(Long id, MultipartFile file, Long currentUserId) throws IOException {
        validateFile(file);
        DsrRequest entity = findActiveOrThrow(id);

        Path uploadDir = Paths.get("uploads", "dsr", String.valueOf(id));
        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        entity.setFilePath(target.toString());
        entity.setFileOriginalName(file.getOriginalFilename());
        entity.setFileMimeType(file.getContentType());

        DsrRequest saved = dsrRepo.save(entity);
        auditService.log("DSR_REQUEST", id, "FILE_UPLOAD", currentUserId,
                null, Map.of("file", file.getOriginalFilename()));
        return toResponse(saved);
    }

    // ── EXPORT ───────────────────────────────────────────────

    public List<DsrRequest> getAllForExport(Status status) {
        return dsrRepo.findAllForExport(status);
    }

    // ── AI ASYNC ─────────────────────────────────────────────

    @Async
    public void enrichWithAiAsync(Long id, String description, String requestType) {
        try {
            DsrRequest entity = dsrRepo.findByIdAndDeletedAtIsNull(id).orElse(null);
            if (entity == null) return;

            var aiDesc = aiClient.describe(description, requestType);
            if (aiDesc != null) {
                entity.setAiDescription((String) aiDesc.get("summary"));
                entity.setIsAiFallback(Boolean.TRUE.equals(aiDesc.get("is_fallback")));
            }

            var aiRec = aiClient.recommend(description, requestType);
            if (aiRec != null) entity.setAiRecommendations(aiRec);

            dsrRepo.save(entity);
            log.info("AI enrichment complete for DSR #{}", id);
        } catch (Exception e) {
            log.error("AI enrichment failed for DSR #{}: {}", id, e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private DsrRequest findActiveOrThrow(Long id) {
        return dsrRepo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("DsrRequest", id));
    }

    private Pageable buildPageable(DsrDto.SearchParams params) {
        Sort sort = "asc".equalsIgnoreCase(params.getDir())
                ? Sort.by(params.getSort()).ascending()
                : Sort.by(params.getSort()).descending();
        return PageRequest.of(params.getPage(), Math.min(params.getSize(), 100), sort);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");
        long maxSize = 10L * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSize) throw new IllegalArgumentException("File exceeds 10 MB limit");
        List<String> allowed = List.of(
                "application/pdf", "image/jpeg", "image/png",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (!allowed.contains(file.getContentType()))
            throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
    }

    private DsrDto.Response toResponse(DsrRequest r) {
        return DsrDto.Response.builder()
                .id(r.getId())
                .uuid(r.getUuid())
                .subjectName(r.getSubjectName())
                .subjectEmail(r.getSubjectEmail())
                .requestType(r.getRequestType())
                .status(r.getStatus())
                .description(r.getDescription())
                .aiDescription(r.getAiDescription())
                .aiRecommendations(r.getAiRecommendations())
                .aiReport(r.getAiReport())
                .isAiFallback(r.getIsAiFallback())
                .priority(r.getPriority())
                .assignedToName(r.getAssignedTo() != null ? r.getAssignedTo().getUsername() : null)
                .deadlineDate(r.getDeadlineDate())
                .resolvedAt(r.getResolvedAt())
                .fileOriginalName(r.getFileOriginalName())
                .hasFile(r.getFilePath() != null)
                .isOverdue(r.isOverdue())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private Map<String, Object> toMap(DsrRequest r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status",      r.getStatus());
        m.put("priority",    r.getPriority());
        m.put("requestType", r.getRequestType());
        m.put("description", r.getDescription());
        return m;
    }
}
