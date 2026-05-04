package com.internship.tool.controller;

import com.internship.tool.entity.Request;
import com.internship.tool.service.RequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests") // ✅ FIXED
public class RequestController {

    @Autowired
    private RequestService requestService;

    // ✅ CREATE REQUEST
    @PostMapping
    public Request createRequest(@RequestBody Request request) {
        request.setStatus("PENDING"); // ensure status is set
        return requestService.createRequest(request);
    }

    // ✅ GET ALL REQUESTS
    @GetMapping
    public Page<Request> getAllRequests(Pageable pageable) {
        return requestService.getAllRequests(pageable);
    }

    // ✅ GET REQUEST BY ID
    @GetMapping("/{id}")
    public Request getRequestById(@PathVariable Long id) {
        return requestService.getRequestById(id);
    }

    // ✅ UPDATE STATUS (approve/reject)
    @PutMapping("/updateStatus/{id}")
    public Request updateStatus(@PathVariable Long id, @RequestParam String status) {
        return requestService.updateStatus(id, status);
    }
}