package com.example.demo.service;

import com.example.demo.dto.CourseDTOs;
import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseService courseService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             UserRepository userRepository,
                             CourseRepository courseRepository,
                             CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<CourseDTOs.CourseDetailDTO> getEnrolledCourses(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId);
        return enrollments.stream().map(e -> {
            CourseDTOs.CourseDetailDTO dto = courseService.toDetailDTO(e.getCourse(), userId);
            dto.setEnrolled(true);
            dto.setUserProgress(e.getProgressPercentage());
            return dto;
        }).collect(Collectors.toList());
    }

    public boolean isEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Transactional
    public Enrollment enroll(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            return enrollmentRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("İstifadəçi tapılmadı: " + userId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Kurs tapılmadı: " + courseId));

        Enrollment enrollment = new Enrollment(user, course);
        return enrollmentRepository.save(enrollment);
    }
}
