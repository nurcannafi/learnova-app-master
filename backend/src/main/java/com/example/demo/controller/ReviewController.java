package com.example.demo.controller;

import com.example.demo.dto.ReviewDTOs;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;

    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ReviewDTOs.ReviewResponse>> getCourseReviews(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getReviewsForCourse(courseId));
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long courseId,
            @Valid @RequestBody ReviewDTOs.ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            ReviewDTOs.ReviewResponse resp = reviewService.addReview(courseId, userDetails.getId(), request);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
