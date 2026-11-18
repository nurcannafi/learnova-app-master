package com.example.demo.repository;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status);

    List<Course> findAllByOrderByCreatedAtDesc();

    Optional<Course> findBySlug(String slug);

    List<Course> findByInstructorId(Long instructorId);

    List<Course> findByInstructorIdOrderByCreatedAtDesc(Long instructorId);

    List<Course> findByCategoryIdAndStatus(Long categoryId, CourseStatus status);

    long countByStatus(CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.status = :status AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Course> searchCourses(@Param("query") String query, @Param("status") CourseStatus status);
}
