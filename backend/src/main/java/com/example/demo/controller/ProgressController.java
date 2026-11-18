package com.example.demo.controller;

import com.example.demo.dto.ProgressDTOs;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@PreAuthorize("isAuthenticated()")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(
            @RequestBody ProgressDTOs.LectureProgressRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            boolean isCompleted = request.getIsCompleted() != null ? request.getIsCompleted() : true;
            ProgressDTOs.CourseProgressResponse resp = progressService.updateLectureProgress(
                    userDetails.getId(), request.getLectureId(), isCompleted);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        return ResponseEntity.ok(progressService.getCourseProgress(userDetails.getId(), courseId));
    }
}
