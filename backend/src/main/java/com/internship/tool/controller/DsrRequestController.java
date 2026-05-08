package com.internship.tool.controller;

import com.internship.tool.dto.DsrDto;
import com.internship.tool.entity.DsrRequest.Status;
import com.internship.tool.service.DsrRequestService;
import com.internship.tool.service.CsvExportService;
import com.internship.tool.config.JwtUtil;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/dsr")
@RequiredArgsConstructor
@Tag(name = "DSR Requests", description = "Data Subject Rights — full CRUD, search, export, and file upload")
@SecurityRequirement(name = "bearerAuth")
public class DsrRequestController {

    private final DsrRequestService dsrService;
    private final CsvExportService  csvExport;
    private final JwtUtil           jwtUtil;

    // ── CREATE ────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Submit a new DSR request",
               description = "Creates a new request and asynchronously enriches it with AI description and recommendations.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<DsrDto.Response> create(
            @Valid @RequestBody DsrDto.CreateRequest body,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = jwtUtil.getUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dsrService.create(body, userId));
    }

    // ── GET ALL (paginated) ───────────────────────────────────

    @GetMapping
    @Operation(summary = "List all DSR requests (paginated)")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<Page<DsrDto.Response>> getAll(
            @RequestParam(defaultValue = "0")          int    page,
            @RequestParam(defaultValue = "20")         int    size,
            @RequestParam(defaultValue = "createdAt")  String sort,
            @RequestParam(defaultValue = "desc")       String dir) {

        DsrDto.SearchParams params = DsrDto.SearchParams.builder()
                .page(page).size(size).sort(sort).dir(dir).build();
        return ResponseEntity.ok(dsrService.getAll(params));
    }

    // ── SEARCH ────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(summary = "Search and filter DSR requests",
               description = "Supports free-text search, status, type, priority, and date range filters.")
    public ResponseEntity<Page<DsrDto.Response>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0")          int    page,
            @RequestParam(defaultValue = "20")         int    size,
            @RequestParam(defaultValue = "createdAt")  String sort,
            @RequestParam(defaultValue = "desc")       String dir) {

        DsrDto.SearchParams params = DsrDto.SearchParams.builder()
                .q(q)
                .status(status   != null ? com.internship.tool.entity.DsrRequest.Status.valueOf(status)           : null)
                .requestType(requestType != null ? com.internship.tool.entity.DsrRequest.RequestType.valueOf(requestType) : null)
                .priority(priority != null ? com.internship.tool.entity.DsrRequest.Priority.valueOf(priority)       : null)
                .from(from).to(to)
                .page(page).size(size).sort(sort).dir(dir)
                .build();
        return ResponseEntity.ok(dsrService.search(params));
    }

    // ── GET BY ID ─────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a single DSR request by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<DsrDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dsrService.getById(id));
    }

    // ── UPDATE ────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update a DSR request")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<DsrDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody DsrDto.UpdateRequest body,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(dsrService.update(id, body, jwtUtil.getUserId(principal)));
    }

    // ── SOFT DELETE ───────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Soft-delete a DSR request (ADMIN/MANAGER only)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        dsrService.delete(id, jwtUtil.getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    // ── STATS ─────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Dashboard KPI statistics")
    public ResponseEntity<DsrDto.StatsResponse> stats() {
        return ResponseEntity.ok(dsrService.getStats());
    }

    // ── CSV EXPORT ────────────────────────────────────────────

    @GetMapping("/export")
    @Operation(summary = "Export DSR requests as CSV",
               description = "Downloads a CSV file. Optional ?status= filter.")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String status) throws IOException {

        Status s = status != null ? Status.valueOf(status.toUpperCase()) : null;
        byte[] csv = csvExport.exportDsr(dsrService.getAllForExport(s));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"dsr_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    // ── FILE UPLOAD ───────────────────────────────────────────

    @PostMapping("/{id}/upload")
    @Operation(summary = "Attach a supporting document to a DSR request",
               description = "Allowed types: PDF, JPEG, PNG, DOC, DOCX. Max size: 10 MB.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File attached"),
        @ApiResponse(responseCode = "400", description = "Invalid file")
    })
    public ResponseEntity<DsrDto.Response> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails principal) throws IOException {

        return ResponseEntity.ok(dsrService.attachFile(id, file, jwtUtil.getUserId(principal)));
    }
}
