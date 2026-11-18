package com.example.demo.controller;

import com.example.demo.dto.CourseDTOs;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<CourseDTOs.CourseSummaryDTO>> getAllCourses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(courseService.getAllPublishedCourses(categoryId, search));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getCourseBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long currentUserId = userDetails != null ? userDetails.getId() : null;
            return ResponseEntity.ok(courseService.getCourseBySlug(slug, currentUserId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getCourseById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long currentUserId = userDetails != null ? userDetails.getId() : null;
            return ResponseEntity.ok(courseService.getCourseById(id, currentUserId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CourseDTOs.CourseCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            CourseDTOs.CourseSummaryDTO created = courseService.createCourse(request, userDetails.getId());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{courseId}/submit")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> submitCourseForReview(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            CourseDTOs.CourseSummaryDTO updated = courseService.submitCourseForReview(courseId, userDetails.getId());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{courseId}/sections")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> addSection(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseDTOs.SectionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            CourseDTOs.SectionDTO created = courseService.addSection(courseId, request, userDetails.getId());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sections/{sectionId}/lectures")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> addLecture(
            @PathVariable Long sectionId,
            @Valid @RequestBody CourseDTOs.LectureCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            CourseDTOs.LectureDTO created = courseService.addLecture(sectionId, request, userDetails.getId());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/instructor/my-courses")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> getInstructorCourses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        return ResponseEntity.ok(courseService.getCoursesByInstructor(userDetails.getId()));
    }
}
