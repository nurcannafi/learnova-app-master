package com.example.demo.service;

import com.example.demo.dto.AdminDTOs;
import com.example.demo.dto.AuthDTOs;
import com.example.demo.dto.CourseDTOs;
import com.example.demo.dto.OrderDTOs;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final InstructorApplicationRepository instructorApplicationRepository;
    private final CourseService courseService;
    private final OrderService orderService;

    public AdminService(UserRepository userRepository,
                        CourseRepository courseRepository,
                        EnrollmentRepository enrollmentRepository,
                        OrderRepository orderRepository,
                        CouponRepository couponRepository,
                        InstructorApplicationRepository instructorApplicationRepository,
                        CourseService courseService,
                        OrderService orderService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.orderRepository = orderRepository;
        this.couponRepository = couponRepository;
        this.instructorApplicationRepository = instructorApplicationRepository;
        this.courseService = courseService;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public AdminDTOs.PlatformStatsResponse getStats() {
        AdminDTOs.PlatformStatsResponse stats = new AdminDTOs.PlatformStatsResponse();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalStudents(userRepository.countByRole(Role.STUDENT));
        stats.setTotalInstructors(userRepository.countByRole(Role.INSTRUCTOR));
        stats.setTotalCourses(courseRepository.count());
        stats.setPublishedCourses(courseRepository.countByStatus(CourseStatus.PUBLISHED));
        stats.setPendingCourses(courseRepository.countByStatus(CourseStatus.PENDING));
        stats.setDraftCourses(courseRepository.countByStatus(CourseStatus.DRAFT));
        stats.setTotalEnrollments(enrollmentRepository.count());
        stats.setTotalOrders(orderRepository.count());

        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<AdminDTOs.AdminUserDTO> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream().map(u -> {
            AdminDTOs.AdminUserDTO dto = new AdminDTOs.AdminUserDTO();
            dto.setId(u.getId());
            dto.setFullName(u.getFullName());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole());
            dto.setCreatedAt(u.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public AdminDTOs.AdminUserDTO updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("İstifadəçi tapılmadı: " + userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);

        AdminDTOs.AdminUserDTO dto = new AdminDTOs.AdminUserDTO();
        dto.setId(saved.getId());
        dto.setFullName(saved.getFullName());
        dto.setEmail(saved.getEmail());
        dto.setRole(saved.getRole());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CourseDTOs.CourseSummaryDTO> getAllCourses(CourseStatus status) {
        List<Course> courses;
        if (status != null) {
            courses = courseRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            courses = courseRepository.findAllByOrderByCreatedAtDesc();
        }
        return courses.stream().map(courseService::toSummaryDTO).collect(Collectors.toList());
    }

    @Transactional
    public CourseDTOs.CourseSummaryDTO updateCourseStatus(Long courseId, CourseStatus newStatus) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Kurs tapılmadı: " + courseId));
        course.setStatus(newStatus);
        Course saved = courseRepository.save(course);
        return courseService.toSummaryDTO(saved);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Kurs tapılmadı: " + courseId);
        }
        courseRepository.deleteById(courseId);
    }

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(o -> {
            OrderDTOs.OrderResponse resp = new OrderDTOs.OrderResponse();
            resp.setId(o.getId());
            resp.setCode(o.getCode());
            resp.setTotalPrice(o.getTotalPrice());
            resp.setStatus(o.getStatus());
            resp.setCreatedAt(o.getCreatedAt());
            if (o.getCoupon() != null) {
                resp.setCouponCode(o.getCoupon().getCode());
            }
            return resp;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCodeIgnoreCase(coupon.getCode())) {
            throw new IllegalArgumentException("Bu kodla kupon artıq mövcuddur: " + coupon.getCode());
        }
        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        couponRepository.deleteById(couponId);
    }

    @Transactional(readOnly = true)
    public List<AuthDTOs.InstructorApplicationDTO> getInstructorApplications(ApplicationStatus status) {
        List<InstructorApplication> apps = status != null
                ? instructorApplicationRepository.findByStatusOrderByCreatedAtDesc(status)
                : instructorApplicationRepository.findAllByOrderByCreatedAtDesc();
        return apps.stream().map(AuthDTOs.InstructorApplicationDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public AuthDTOs.InstructorApplicationDTO approveInstructorApplication(Long applicationId) {
        InstructorApplication app = instructorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Müraciət tapılmadı: " + applicationId));
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException("Yalnız gözləmədə olan müraciətlər təsdiqlənə bilər");
        }
        User user = app.getUser();
        user.setRole(Role.INSTRUCTOR);
        userRepository.save(user);
        app.setStatus(ApplicationStatus.APPROVED);
        app.setReviewedAt(java.time.LocalDateTime.now());
        return new AuthDTOs.InstructorApplicationDTO(instructorApplicationRepository.save(app));
    }

    @Transactional
    public AuthDTOs.InstructorApplicationDTO rejectInstructorApplication(Long applicationId) {
        InstructorApplication app = instructorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Müraciət tapılmadı: " + applicationId));
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException("Yalnız gözləmədə olan müraciətlər imtina edilə bilər");
        }
        app.setStatus(ApplicationStatus.REJECTED);
        app.setReviewedAt(java.time.LocalDateTime.now());
        return new AuthDTOs.InstructorApplicationDTO(instructorApplicationRepository.save(app));
    }
}
