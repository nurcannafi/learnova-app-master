package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    Optional<Review> findByUserIdAndCourseId(Long userId, Long courseId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double getAverageRatingForCourse(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);
}
