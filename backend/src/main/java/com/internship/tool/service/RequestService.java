package com.internship.tool.service;

import com.internship.tool.entity.Request;
import com.internship.tool.repository.RequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {

    @Autowired
    private RequestRepository repository;

    public List<Request> getAllRequests() {
        return repository.findAll();
    }

    public Request saveRequest(Request request) {
        return repository.save(request);
    }
}