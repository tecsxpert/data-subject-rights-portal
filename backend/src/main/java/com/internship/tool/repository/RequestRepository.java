package com.internship.tool.repository;

import com.internship.tool.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // 🔥 ADD THIS METHOD
    List<Request> findByEmail(String email);
}