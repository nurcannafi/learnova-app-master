package com.example.demo.dto;

import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.InstructorApplication;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AuthDTOs {

    public static class RegisterRequest {
        @NotBlank(message = "Ad və soyad tələb olunur")
        private String fullName;

        @NotBlank(message = "Email tələb olunur")
        @Email(message = "Düzgün email formatı daxil edin")
        private String email;

        @NotBlank(message = "Şifrə tələb olunur")
        @Size(min = 6, message = "Şifrə ən azı 6 simvol olmalıdır")
        private String password;

        @NotBlank(message = "Şifrə təsdiqi tələb olunur")
        private String confirmPassword;

        // Ignored server-side — registration always assigns STUDENT
        private Role role;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    public static class LoginRequest {
        @NotBlank(message = "Email tələb olunur")
        private String email;

        @NotBlank(message = "Şifrə tələb olunur")
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class InstructorApplyRequest {
        @NotBlank(message = "Qısa bio / təcrübə məlumatı tələb olunur")
        @Size(min = 20, max = 2000, message = "Bio 20-2000 simvol arasında olmalıdır")
        private String bio;

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
    }

    public static class UserDTO {
        private Long id;
        private String fullName;
        private String email;
        private Role role;

        public UserDTO() {}
        public UserDTO(User user) {
            this.id = user.getId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.role = user.getRole();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    public static class AuthResponse {
        private String token;
        private UserDTO user;

        public AuthResponse(String token, UserDTO user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public UserDTO getUser() { return user; }
        public void setUser(UserDTO user) { this.user = user; }
    }

    public static class InstructorApplicationDTO {
        private Long id;
        private Long userId;
        private String fullName;
        private String email;
        private String bio;
        private ApplicationStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime reviewedAt;

        public InstructorApplicationDTO() {}

        public InstructorApplicationDTO(InstructorApplication app) {
            this.id = app.getId();
            this.userId = app.getUser().getId();
            this.fullName = app.getUser().getFullName();
            this.email = app.getUser().getEmail();
            this.bio = app.getBio();
            this.status = app.getStatus();
            this.createdAt = app.getCreatedAt();
            this.reviewedAt = app.getReviewedAt();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getReviewedAt() { return reviewedAt; }
        public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    }
}
