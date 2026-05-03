package com.internship.tool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // EMAIL WHEN REQUEST IS CREATED
    public void sendRequestMail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("DSR Request Submitted");
        message.setText("Your data subject request has been successfully submitted and is currently under review.");

        mailSender.send(message);
    }

    // EMAIL WHEN STATUS IS UPDATED
    public void sendStatusUpdateMail(String toEmail, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("DSR Request Status Update");
        message.setText("Your request status has been updated to: " + status);

        mailSender.send(message);
    }
}