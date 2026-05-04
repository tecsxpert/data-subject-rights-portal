package com.internship.tool.controller;

import com.internship.tool.entity.Request;
import com.internship.tool.repository.RequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin("*")
public class RequestController {

    @Autowired
    private RequestRepository requestRepository;

    // ✅ GET ALL REQUESTS
    @GetMapping
    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    // ✅ CREATE REQUEST
    @PostMapping
    public Request createRequest(@RequestBody Request request) {
        request.setStatus("PENDING");
        return requestRepository.save(request);
    }

    // ✅ UPDATE STATUS (APPROVE / REJECT)
    @PutMapping("/{id}")
    public Request updateStatus(@PathVariable Long id, @RequestParam String status) {

        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        req.setStatus(status);

        return requestRepository.save(req);
    }
}