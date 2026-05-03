package com.internship.tool.service;

import com.internship.tool.entity.Request;
import com.internship.tool.repository.RequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RequestService {

    @Autowired
    private RequestRepository repository;

    @Autowired
    private EmailService emailService;

    // ✅ CREATE REQUEST + DEFAULT STATUS + EMAIL
    public Request createRequest(Request request) {
        request.setStatus("PENDING"); // default status

        Request savedRequest = repository.save(request);

        // send email after saving
        emailService.sendRequestMail(savedRequest.getEmail());

        return savedRequest;
    }

    // ✅ GET ALL WITH PAGINATION
    public Page<Request> getAllRequests(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // ✅ GET BY ID (WITH ERROR HANDLING)
    public Request getRequestById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + id));
    }

    // ✅ UPDATE STATUS (NEW - DAY 8 MAIN LOGIC)
    public Request updateStatus(Long id, String status) {
        Request request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + id));

        request.setStatus(status);

        Request updatedRequest = repository.save(request);

        // 🔥 SEND STATUS UPDATE EMAIL
        emailService.sendStatusUpdateMail(updatedRequest.getEmail(), status);

        return updatedRequest;
    }
}