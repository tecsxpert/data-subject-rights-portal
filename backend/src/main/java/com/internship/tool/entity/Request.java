package com.internship.tool.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String requestType;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String status = "PENDING";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
}
@PrePersist
public void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}

@PreUpdate
public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}
