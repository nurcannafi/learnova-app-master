package com.example.demo.repository;

import com.example.demo.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findBySectionIdOrderByOrderIndexAsc(Long sectionId);
    
    long countBySectionCourseId(Long courseId);
}
