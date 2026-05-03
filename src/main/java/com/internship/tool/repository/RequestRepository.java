package com.internship.tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.internship.tool.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
}
