package com.internship.tool.controller;

import com.internship.tool.entity.Request;
import com.internship.tool.service.RequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    // CREATE REQUEST
    @PostMapping
    public Request createRequest(@RequestBody Request request) {
        return requestService.createRequest(request);
    }

    // GET ALL REQUESTS (PAGINATION)
    @GetMapping
    public Page<Request> getAllRequests(Pageable pageable) {
        return requestService.getAllRequests(pageable);
    }

    // GET REQUEST BY ID
    @GetMapping("/{id}")
    public Request getRequestById(@PathVariable Long id) {
        return requestService.getRequestById(id);
    }

    // UPDATE STATUS
    @PutMapping("/updateStatus/{id}")
    public Request updateStatus(@PathVariable Long id, @RequestParam String status) {
        return requestService.updateStatus(id, status);
    }
}