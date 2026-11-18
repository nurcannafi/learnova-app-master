package com.example.demo.controller;

import com.example.demo.dto.AdminDTOs;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.Coupon;
import com.example.demo.entity.CourseStatus;
import com.example.demo.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody AdminDTOs.UserRoleUpdateRequest request) {
        try {
            return ResponseEntity.ok(adminService.updateUserRole(id, request.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses(@RequestParam(required = false) CourseStatus status) {
        return ResponseEntity.ok(adminService.getAllCourses(status));
    }

    @PutMapping("/courses/{id}/status")
    public ResponseEntity<?> updateCourseStatus(
            @PathVariable Long id,
            @RequestBody AdminDTOs.CourseStatusUpdateRequest request) {
        try {
            return ResponseEntity.ok(adminService.updateCourseStatus(id, request.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            adminService.deleteCourse(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @GetMapping("/coupons")
    public ResponseEntity<?> getAllCoupons() {
        return ResponseEntity.ok(adminService.getAllCoupons());
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        try {
            return ResponseEntity.ok(adminService.createCoupon(coupon));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        adminService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/instructor-applications")
    public ResponseEntity<?> getInstructorApplications(
            @RequestParam(required = false) ApplicationStatus status) {
        return ResponseEntity.ok(adminService.getInstructorApplications(status));
    }

    @PutMapping("/instructor-applications/{id}/approve")
    public ResponseEntity<?> approveInstructorApplication(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.approveInstructorApplication(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/instructor-applications/{id}/reject")
    public ResponseEntity<?> rejectInstructorApplication(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.rejectInstructorApplication(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
