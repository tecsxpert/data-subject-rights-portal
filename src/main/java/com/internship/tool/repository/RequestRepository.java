package com.internship.tool.repository;

import com.internship.tool.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByStatus(String status);

    List<Request> findByEmail(String email);
}
