package com.example.demo.controller;

import com.example.demo.entity.Enrollment;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@PreAuthorize("isAuthenticated()")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyEnrolledCourses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        return ResponseEntity.ok(enrollmentService.getEnrolledCourses(userDetails.getId()));
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<?> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            Enrollment enrollment = enrollmentService.enroll(userDetails.getId(), courseId);
            return ResponseEntity.ok(enrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
