package com.example.demo.repository;

import com.example.demo.entity.LectureProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureProgressRepository extends JpaRepository<LectureProgress, Long> {
    Optional<LectureProgress> findByUserIdAndLectureId(Long userId, Long lectureId);
    
    List<LectureProgress> findByUserIdAndLectureSectionCourseId(Long userId, Long courseId);

    @Query("SELECT COUNT(lp) FROM LectureProgress lp WHERE lp.user.id = :userId AND lp.lecture.section.course.id = :courseId AND lp.isCompleted = true")
    long countCompletedByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
