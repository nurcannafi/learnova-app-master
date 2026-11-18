package com.example.demo.service;

import com.example.demo.dto.ReviewDTOs;
import com.example.demo.entity.Course;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         CourseRepository courseRepository,
                         UserRepository userRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.reviewRepository = reviewRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<ReviewDTOs.ReviewResponse> getReviewsForCourse(Long courseId) {
        return reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTOs.ReviewResponse addReview(Long courseId, Long userId, ReviewDTOs.ReviewRequest request) {
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("Yalnız kursa yazılan tələbələr rəy yaza bilər");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("İstifadəçi tapılmadı"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Kurs tapılmadı"));

        Review review = reviewRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> new Review(user, course, request.getRating(), request.getComment()));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    private ReviewDTOs.ReviewResponse toResponse(Review r) {
        ReviewDTOs.ReviewResponse resp = new ReviewDTOs.ReviewResponse();
        resp.setId(r.getId());
        resp.setUserId(r.getUser().getId());
        resp.setUserName(r.getUser().getFullName());
        resp.setRating(r.getRating());
        resp.setComment(r.getComment());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}
