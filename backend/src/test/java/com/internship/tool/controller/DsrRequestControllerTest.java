package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.dto.DsrDto;
import com.internship.tool.entity.DsrRequest.Priority;
import com.internship.tool.entity.DsrRequest.RequestType;
import com.internship.tool.entity.DsrRequest.Status;
import com.internship.tool.service.DsrRequestService;
import com.internship.tool.service.CsvExportService;
import com.internship.tool.config.JwtUtil;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DsrRequestController.class)
@DisplayName("DSR Request Controller — MockMvc Tests")
class DsrRequestControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean DsrRequestService dsrService;
    @MockBean CsvExportService  csvExport;
    @MockBean JwtUtil           jwtUtil;

    private static DsrDto.Response sampleResponse() {
        return DsrDto.Response.builder()
                .id(1L).uuid(UUID.randomUUID())
                .subjectName("Test User").subjectEmail("test@example.com")
                .requestType(RequestType.ACCESS).status(Status.PENDING)
                .priority(Priority.MEDIUM).isAiFallback(false)
                .isOverdue(false).hasFile(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    // ── POST /api/v1/dsr ──────────────────────────────────────

    @Test @WithMockUser(roles = "USER")
    @DisplayName("POST /dsr → 201 Created with valid payload")
    void createDsr_validPayload_returns201() throws Exception {
        DsrDto.CreateRequest body = DsrDto.CreateRequest.builder()
                .subjectName("Test User").subjectEmail("test@example.com")
                .requestType(RequestType.ACCESS).description("I need my data.")
                .build();

        Mockito.when(dsrService.create(any(), any())).thenReturn(sampleResponse());
        Mockito.when(jwtUtil.getUserId(any())).thenReturn(1L);

        mvc.perform(post("/api/v1/dsr").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test @WithMockUser(roles = "USER")
    @DisplayName("POST /dsr → 400 Bad Request when email is invalid")
    void createDsr_invalidEmail_returns400() throws Exception {
        DsrDto.CreateRequest body = DsrDto.CreateRequest.builder()
                .subjectName("Test").subjectEmail("not-an-email")
                .requestType(RequestType.ACCESS).build();

        mvc.perform(post("/api/v1/dsr").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles = "USER")
    @DisplayName("POST /dsr → 400 Bad Request when subject name is blank")
    void createDsr_blankName_returns400() throws Exception {
        DsrDto.CreateRequest body = DsrDto.CreateRequest.builder()
                .subjectName("  ").subjectEmail("test@example.com")
                .requestType(RequestType.ACCESS).build();

        mvc.perform(post("/api/v1/dsr").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/v1/dsr ───────────────────────────────────────

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /dsr → 200 OK with paginated results")
    void getAll_returns200WithPage() throws Exception {
        Page<DsrDto.Response> page = new PageImpl<>(List.of(sampleResponse()),
                PageRequest.of(0, 20), 1);
        Mockito.when(dsrService.getAll(any())).thenReturn(page);

        mvc.perform(get("/api/v1/dsr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].subjectName").value("Test User"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /dsr → 401 Unauthorized without token")
    void getAll_noToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/dsr"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/dsr/{id} ─────────────────────────────────

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /dsr/{id} → 200 OK for existing record")
    void getById_exists_returns200() throws Exception {
        Mockito.when(dsrService.getById(1L)).thenReturn(sampleResponse());

        mvc.perform(get("/api/v1/dsr/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /dsr/{id} → 404 Not Found for missing record")
    void getById_notFound_returns404() throws Exception {
        Mockito.when(dsrService.getById(99L))
                .thenThrow(new com.internship.tool.exception.ResourceNotFoundException("DsrRequest", 99L));

        mvc.perform(get("/api/v1/dsr/99"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/v1/dsr/{id} ─────────────────────────────────

    @Test @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /dsr/{id} → 200 OK with valid update")
    void update_validPayload_returns200() throws Exception {
        DsrDto.UpdateRequest body = DsrDto.UpdateRequest.builder()
                .status(Status.IN_PROGRESS).build();
        Mockito.when(dsrService.update(eq(1L), any(), any())).thenReturn(sampleResponse());
        Mockito.when(jwtUtil.getUserId(any())).thenReturn(2L);

        mvc.perform(put("/api/v1/dsr/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/v1/dsr/{id} ──────────────────────────────

    @Test @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /dsr/{id} → 204 No Content for ADMIN")
    void delete_asAdmin_returns204() throws Exception {
        Mockito.when(jwtUtil.getUserId(any())).thenReturn(1L);

        mvc.perform(delete("/api/v1/dsr/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test @WithMockUser(roles = "USER")
    @DisplayName("DELETE /dsr/{id} → 403 Forbidden for USER role")
    void delete_asUser_returns403() throws Exception {
        mvc.perform(delete("/api/v1/dsr/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/v1/dsr/stats ────────────────────────────────

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /dsr/stats → 200 OK with KPI data")
    void stats_returns200() throws Exception {
        DsrDto.StatsResponse stats = DsrDto.StatsResponse.builder()
                .total(30).pending(10).inProgress(8).completed(10)
                .rejected(2).overdue(3).newThisWeek(5).newThisMonth(15)
                .avgResolutionHours(48.5).build();
        Mockito.when(dsrService.getStats()).thenReturn(stats);

        mvc.perform(get("/api/v1/dsr/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(30))
                .andExpect(jsonPath("$.pending").value(10))
                .andExpect(jsonPath("$.overdue").value(3));
    }

    // ── GET /api/v1/dsr/export ───────────────────────────────

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /dsr/export → 200 OK with CSV content-type")
    void export_returnsCsv() throws Exception {
        Mockito.when(dsrService.getAllForExport(null)).thenReturn(List.of());
        Mockito.when(csvExport.exportDsr(any())).thenReturn("ID,UUID\n".getBytes());

        mvc.perform(get("/api/v1/dsr/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    // ── GET /api/v1/dsr/search ───────────────────────────────

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /dsr/search → 200 OK with query param")
    void search_withQuery_returns200() throws Exception {
        Page<DsrDto.Response> page = new PageImpl<>(List.of(sampleResponse()));
        Mockito.when(dsrService.search(any())).thenReturn(page);

        mvc.perform(get("/api/v1/dsr/search?q=priya&status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
