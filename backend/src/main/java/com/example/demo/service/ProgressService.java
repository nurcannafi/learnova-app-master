package com.example.demo.service;

import com.example.demo.dto.ProgressDTOs;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProgressService {

    private final LectureProgressRepository lectureProgressRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;

    public ProgressService(LectureProgressRepository lectureProgressRepository,
                           LectureRepository lectureRepository,
                           UserRepository userRepository,
                           EnrollmentRepository enrollmentRepository,
                           CertificateRepository certificateRepository) {
        this.lectureProgressRepository = lectureProgressRepository;
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.certificateRepository = certificateRepository;
    }

    @Transactional
    public ProgressDTOs.CourseProgressResponse updateLectureProgress(Long userId, Long lectureId, boolean isCompleted) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("İstifadəçi tapılmadı: " + userId));

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("Dərs tapılmadı: " + lectureId));

        Course course = lecture.getSection().getCourse();

        // Check enrollment
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                .orElseThrow(() -> new IllegalArgumentException("Siz bu kursa yazılmamısınız"));

        LectureProgress progress = lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId)
                .orElseGet(() -> new LectureProgress(user, lecture, isCompleted));

        progress.setIsCompleted(isCompleted);
        lectureProgressRepository.save(progress);

        // Recalculate course progress
        long totalLectures = lectureRepository.countBySectionCourseId(course.getId());
        long completedLectures = lectureProgressRepository.countCompletedByUserIdAndCourseId(userId, course.getId());

        int percentage = totalLectures > 0 ? (int) Math.round(((double) completedLectures / totalLectures) * 100) : 0;
        if (percentage > 100) percentage = 100;

        enrollment.setProgressPercentage(percentage);
        enrollmentRepository.save(enrollment);

        // If 100%, generate certificate if not already generated
        String certCode = null;
        if (percentage == 100) {
            Optional<Certificate> existingCert = certificateRepository.findByUserIdAndCourseId(userId, course.getId());
            if (existingCert.isEmpty()) {
                String code = "LNV-" + course.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Certificate cert = new Certificate(user, course, code);
                Certificate savedCert = certificateRepository.save(cert);
                certCode = savedCert.getCertificateCode();
            } else {
                certCode = existingCert.get().getCertificateCode();
            }
        }

        ProgressDTOs.CourseProgressResponse response = new ProgressDTOs.CourseProgressResponse();
        response.setCourseId(course.getId());
        response.setCourseTitle(course.getTitle());
        response.setTotalLectures(totalLectures);
        response.setCompletedLectures(completedLectures);
        response.setProgressPercentage(percentage);
        response.setCompleted(percentage == 100);
        response.setCertificateCode(certCode);

        return response;
    }

    public ProgressDTOs.CourseProgressResponse getCourseProgress(Long userId, Long courseId) {
        long totalLectures = lectureRepository.countBySectionCourseId(courseId);
        long completedLectures = lectureProgressRepository.countCompletedByUserIdAndCourseId(userId, courseId);

        int percentage = totalLectures > 0 ? (int) Math.round(((double) completedLectures / totalLectures) * 100) : 0;
        if (percentage > 100) percentage = 100;

        Optional<Certificate> certOpt = certificateRepository.findByUserIdAndCourseId(userId, courseId);

        ProgressDTOs.CourseProgressResponse response = new ProgressDTOs.CourseProgressResponse();
        response.setCourseId(courseId);
        response.setTotalLectures(totalLectures);
        response.setCompletedLectures(completedLectures);
        response.setProgressPercentage(percentage);
        response.setCompleted(percentage == 100);
        certOpt.ifPresent(certificate -> response.setCertificateCode(certificate.getCertificateCode()));

        return response;
    }
}
