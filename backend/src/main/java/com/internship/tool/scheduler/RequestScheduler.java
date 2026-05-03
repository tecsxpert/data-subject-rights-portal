package com.internship.tool.scheduler;

import com.internship.tool.entity.Request;
import com.internship.tool.repository.RequestRepository;
import com.internship.tool.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestScheduler {

    @Autowired
    private RequestRepository repository;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 60000)
    public void processRequests() {

        System.out.println("🔄 Scheduler running...");

        List<Request> requests = repository.findAll();

        for (Request req : requests) {

            if ("PENDING".equals(req.getStatus())) {

                req.setStatus("APPROVED");
                repository.save(req);

                
                
                System.out.println("✅ Approved: " + req.getEmail());
            }
        }
    }
}