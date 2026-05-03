package com.internship.tool.controller;

import com.internship.tool.entity.Request;
import com.internship.tool.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @GetMapping("/all")
    public List<Request> getAllRequests() {
        return requestService.getAllRequests();
    }

    @PostMapping("/create")
    public Request createRequest(@RequestBody Request request) {
        return requestService.createRequest(request);
    }

    @PutMapping("/{id}/status")
    public Request updateStatus(@PathVariable Long id,
                                @RequestParam String status) {
        return requestService.updateStatus(id, status);
    }
}