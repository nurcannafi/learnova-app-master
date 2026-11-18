package com.example.demo.repository;

import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.InstructorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, Long> {
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);
    Optional<InstructorApplication> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    List<InstructorApplication> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);
    List<InstructorApplication> findAllByOrderByCreatedAtDesc();
}
