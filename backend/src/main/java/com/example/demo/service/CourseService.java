package com.example.demo.service;

import com.example.demo.dto.CourseDTOs;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final ReviewRepository reviewRepository;

    public CourseService(CourseRepository courseRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         SectionRepository sectionRepository,
                         LectureRepository lectureRepository,
                         EnrollmentRepository enrollmentRepository,
                         LectureProgressRepository lectureProgressRepository,
                         ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.lectureRepository = lectureRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lectureProgressRepository = lectureProgressRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseDTOs.CourseSummaryDTO> getAllPublishedCourses(Long categoryId, String search) {
        List<Course> courses;
        if (search != null && !search.trim().isEmpty()) {
            courses = courseRepository.searchCourses(search.trim(), CourseStatus.PUBLISHED);
        } else if (categoryId != null) {
            courses = courseRepository.findByCategoryIdAndStatus(categoryId, CourseStatus.PUBLISHED);
        } else {
            courses = courseRepository.findByStatus(CourseStatus.PUBLISHED);
        }
        return courses.stream().map(this::toSummaryDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTOs.CourseSummaryDTO> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorIdOrderByCreatedAtDesc(instructorId)
                .stream().map(this::toSummaryDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDTOs.CourseDetailDTO getCourseBySlug(String slug, Long currentUserId) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Kurs tapılmadı: " + slug));

        // If not published, only the instructor or admin can view
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            if (currentUserId == null || !course.getInstructor().getId().equals(currentUserId)) {
                User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
                if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                    throw new IllegalArgumentException("Bu kurs hələ dərc edilməyib");
                }
            }
        }

        return toDetailDTO(course, currentUserId);
    }

    @Transactional(readOnly = true)
    public CourseDTOs.CourseDetailDTO getCourseById(Long id, Long currentUserId) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kurs tapılmadı: " + id));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            if (currentUserId == null || !course.getInstructor().getId().equals(currentUserId)) {
                User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
                if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                    throw new IllegalArgumentException("Bu kurs hələ dərc edilməyib");
                }
            }
        }

        return toDetailDTO(course, currentUserId);
    }

    @Transactional
    public CourseDTOs.CourseSummaryDTO createCourse(CourseDTOs.CourseCreateRequest request, Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Müəllim tapılmadı: " + instructorId));

        if (instructor.getRole() != Role.INSTRUCTOR && instructor.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Yalnız müəllimlər və inzibatçılar kurs yarada bilər");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Kateqoriya tapılmadı: " + request.getCategoryId()));

        String slug = generateSlug(request.getTitle());

        // Default status for new course is DRAFT
        Course course = new Course(
                request.getTitle().trim(),
                slug,
                request.getPrice(),
                request.getLevel() != null ? request.getLevel() : CourseLevel.BEGINNER,
                CourseStatus.DRAFT,
                category,
                instructor,
                request.getDescription(),
                request.getThumbnailUrl()
        );

        Course saved = courseRepository.save(course);
        return toSummaryDTO(saved);
    }

    @Transactional
    public CourseDTOs.CourseSummaryDTO submitCourseForReview(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Kurs tapılmadı: " + courseId));

        if (!course.getInstructor().getId().equals(instructorId)) {
            User user = userRepository.findById(instructorId).orElse(null);
            if (user == null || user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Yalnız kursun müəllifi kursu təsdiqə göndərə bilər");
            }
        }

        course.setStatus(CourseStatus.PENDING);
        Course updated = courseRepository.save(course);
        return toSummaryDTO(updated);
    }

    @Transactional
    public CourseDTOs.SectionDTO addSection(Long courseId, CourseDTOs.SectionCreateRequest request, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Kurs tapılmadı: " + courseId));

        if (!course.getInstructor().getId().equals(instructorId)) {
            User user = userRepository.findById(instructorId).orElse(null);
            if (user == null || user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Yalnız kursun müəllimi bölmə əlavə edə bilər");
            }
        }

        Section section = new Section(course, request.getTitle().trim(), request.getOrderIndex());
        Section saved = sectionRepository.save(section);

        CourseDTOs.SectionDTO dto = new CourseDTOs.SectionDTO();
        dto.setId(saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setOrderIndex(saved.getOrderIndex());
        return dto;
    }

    @Transactional
    public CourseDTOs.LectureDTO addLecture(Long sectionId, CourseDTOs.LectureCreateRequest request, Long instructorId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Bölmə tapılmadı: " + sectionId));

        if (!section.getCourse().getInstructor().getId().equals(instructorId)) {
            User user = userRepository.findById(instructorId).orElse(null);
            if (user == null || user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Yalnız kursun müəllimi dərs əlavə edə bilər");
            }
        }

        ContentType contentType = request.getContentType() != null ? request.getContentType() : ContentType.VIDEO;
        String contentUrl = request.getContentUrl() != null ? request.getContentUrl().trim() : null;
        if (contentType == ContentType.VIDEO && (contentUrl == null || contentUrl.isBlank())) {
            throw new IllegalArgumentException("Video dərslər üçün məzmun URL-i məcburidir");
        }

        Lecture lecture = new Lecture(
                section,
                request.getTitle().trim(),
                contentType,
                contentUrl,
                request.getTextContent(),
                request.getDurationMinutes(),
                request.getOrderIndex()
        );

        Lecture saved = lectureRepository.save(lecture);

        CourseDTOs.LectureDTO dto = new CourseDTOs.LectureDTO();
        dto.setId(saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setContentType(saved.getContentType());
        dto.setContentUrl(saved.getContentUrl());
        dto.setTextContent(saved.getTextContent());
        dto.setDurationMinutes(saved.getDurationMinutes());
        dto.setOrderIndex(saved.getOrderIndex());
        dto.setIsCompleted(false);
        return dto;
    }

    public CourseDTOs.CourseSummaryDTO toSummaryDTO(Course c) {
        CourseDTOs.CourseSummaryDTO dto = new CourseDTOs.CourseSummaryDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setSlug(c.getSlug());
        dto.setPrice(c.getPrice());
        dto.setLevel(c.getLevel());
        dto.setStatus(c.getStatus());
        dto.setDescription(c.getDescription());
        dto.setThumbnailUrl(c.getThumbnailUrl());

        if (c.getCategory() != null) {
            dto.setCategoryId(c.getCategory().getId());
            dto.setCategoryName(c.getCategory().getName());
        }

        if (c.getInstructor() != null) {
            dto.setInstructorId(c.getInstructor().getId());
            dto.setInstructorName(c.getInstructor().getFullName());
        }

        Double avgRating = reviewRepository.getAverageRatingForCourse(c.getId());
        dto.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0);
        dto.setTotalReviews(reviewRepository.countByCourseId(c.getId()));
        dto.setTotalStudents(enrollmentRepository.countByCourseId(c.getId()));

        return dto;
    }

    public CourseDTOs.CourseDetailDTO toDetailDTO(Course c, Long currentUserId) {
        CourseDTOs.CourseDetailDTO dto = new CourseDTOs.CourseDetailDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setSlug(c.getSlug());
        dto.setPrice(c.getPrice());
        dto.setLevel(c.getLevel());
        dto.setStatus(c.getStatus());
        dto.setDescription(c.getDescription());
        dto.setThumbnailUrl(c.getThumbnailUrl());

        if (c.getCategory() != null) {
            dto.setCategoryId(c.getCategory().getId());
            dto.setCategoryName(c.getCategory().getName());
        }

        if (c.getInstructor() != null) {
            dto.setInstructorId(c.getInstructor().getId());
            dto.setInstructorName(c.getInstructor().getFullName());
        }

        Double avgRating = reviewRepository.getAverageRatingForCourse(c.getId());
        dto.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0);
        dto.setTotalReviews(reviewRepository.countByCourseId(c.getId()));
        dto.setTotalStudents(enrollmentRepository.countByCourseId(c.getId()));

        // Check if enrolled
        if (currentUserId != null) {
            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserIdAndCourseId(currentUserId, c.getId());
            if (enrollmentOpt.isPresent()) {
                dto.setEnrolled(true);
                dto.setUserProgress(enrollmentOpt.get().getProgressPercentage());
            }
        }

        // Map sections & lectures
        List<Section> sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(c.getId());
        Set<Long> completedLectureIds = new HashSet<>();
        if (currentUserId != null) {
            List<LectureProgress> progressList = lectureProgressRepository.findByUserIdAndLectureSectionCourseId(currentUserId, c.getId());
            for (LectureProgress lp : progressList) {
                if (Boolean.TRUE.equals(lp.getIsCompleted())) {
                    completedLectureIds.add(lp.getLecture().getId());
                }
            }
        }

        List<CourseDTOs.SectionDTO> sectionDTOs = new ArrayList<>();
        for (Section s : sections) {
            CourseDTOs.SectionDTO sDto = new CourseDTOs.SectionDTO();
            sDto.setId(s.getId());
            sDto.setTitle(s.getTitle());
            sDto.setOrderIndex(s.getOrderIndex());

            List<Lecture> lectures = lectureRepository.findBySectionIdOrderByOrderIndexAsc(s.getId());
            List<CourseDTOs.LectureDTO> lectureDTOs = new ArrayList<>();
            for (Lecture l : lectures) {
                CourseDTOs.LectureDTO lDto = new CourseDTOs.LectureDTO();
                lDto.setId(l.getId());
                lDto.setTitle(l.getTitle());
                lDto.setContentType(l.getContentType());
                lDto.setContentUrl(l.getContentUrl());
                lDto.setTextContent(l.getTextContent());
                lDto.setDurationMinutes(l.getDurationMinutes());
                lDto.setOrderIndex(l.getOrderIndex());
                lDto.setIsCompleted(completedLectureIds.contains(l.getId()));
                lectureDTOs.add(lDto);
            }
            sDto.setLectures(lectureDTOs);
            sectionDTOs.add(sDto);
        }
        dto.setSections(sectionDTOs);

        return dto;
    }

    private String generateSlug(String title) {
        String base = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        String slug = base;
        int count = 1;
        while (courseRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + (++count);
        }
        return slug;
    }
}
