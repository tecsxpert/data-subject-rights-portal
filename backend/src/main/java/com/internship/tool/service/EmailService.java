package com.internship.tool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ✅ Email when request is created
    public void sendRequestMail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("DSR Request Submitted");
            message.setText("Your request has been successfully submitted and is in PENDING status.");

            mailSender.send(message);

            System.out.println("✅ Request Email sent to: " + toEmail);

        } catch (Exception e) {
            System.out.println("❌ Request Email failed: " + e.getMessage());
        }
    }

    // ✅ Email when request is approved (NEW)
    public void sendApprovalMail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("DSR Request Approved");
            message.setText("Your request has been APPROVED successfully.");

            mailSender.send(message);

            System.out.println("✅ Approval Email sent to: " + toEmail);

        } catch (Exception e) {
            System.out.println("❌ Approval Email failed: " + e.getMessage());
        }
    }
}