package com.internship.tool.controller;

import com.internship.tool.entity.Request;
import com.internship.tool.service.RequestService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    // ✅ GET ALL (Pagination)
    @GetMapping("/all")
    public ResponseEntity<Page<Request>> getAllRequests(Pageable pageable) {
        return ResponseEntity.ok(requestService.getAllRequests(pageable));
    }

    // ✅ GET BY ID
    @GetMapping("/id/{id}")
    public ResponseEntity<Request> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    // ✅ CREATE REQUEST
    @PostMapping("/create")
    public ResponseEntity<Request> createRequest(@Valid @RequestBody Request request) {
        Request saved = requestService.createRequest(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}