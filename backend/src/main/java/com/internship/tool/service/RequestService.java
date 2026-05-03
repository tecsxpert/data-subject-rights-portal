package com.internship.tool.service;

import com.internship.tool.entity.Request;
import com.internship.tool.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    public Request updateStatus(Long id, String status) {
        Request req = requestRepository.findById(id).orElseThrow();
        req.setStatus(status);
        return requestRepository.save(req);
    }

    public Request createRequest(Request request) {
        request.setStatus("PENDING");
        return requestRepository.save(request);
    }
}