package com.example.demo.dto;

import com.example.demo.entity.CourseStatus;
import com.example.demo.entity.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminDTOs {

    public static class PlatformStatsResponse {
        private long totalUsers;
        private long totalStudents;
        private long totalInstructors;
        private long totalCourses;
        private long publishedCourses;
        private long pendingCourses;
        private long draftCourses;
        private long totalEnrollments;
        private long totalOrders;
        private BigDecimal totalRevenue;

        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
        public long getTotalInstructors() { return totalInstructors; }
        public void setTotalInstructors(long totalInstructors) { this.totalInstructors = totalInstructors; }
        public long getTotalCourses() { return totalCourses; }
        public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }
        public long getPublishedCourses() { return publishedCourses; }
        public void setPublishedCourses(long publishedCourses) { this.publishedCourses = publishedCourses; }
        public long getPendingCourses() { return pendingCourses; }
        public void setPendingCourses(long pendingCourses) { this.pendingCourses = pendingCourses; }
        public long getDraftCourses() { return draftCourses; }
        public void setDraftCourses(long draftCourses) { this.draftCourses = draftCourses; }
        public long getTotalEnrollments() { return totalEnrollments; }
        public void setTotalEnrollments(long totalEnrollments) { this.totalEnrollments = totalEnrollments; }
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class AdminUserDTO {
        private Long id;
        private String fullName;
        private String email;
        private Role role;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CourseStatusUpdateRequest {
        private CourseStatus status;

        public CourseStatus getStatus() { return status; }
        public void setStatus(CourseStatus status) { this.status = status; }
    }

    public static class UserRoleUpdateRequest {
        private Role role;

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }
}
