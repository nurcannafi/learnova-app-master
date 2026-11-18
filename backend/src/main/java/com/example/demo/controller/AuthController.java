package com.example.demo.controller;

import com.example.demo.dto.AuthDTOs;
import com.example.demo.entity.User;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(org.springframework.context.support.DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Daxil edilən məlumatlar düzgün deyil");
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericExceptions(Exception ex) {
        return ResponseEntity.badRequest().body(ex.getMessage() != null ? ex.getMessage() : "Xəta baş verdi");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        try {
            AuthDTOs.AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        try {
            AuthDTOs.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş edilməyib");
        }
        try {
            User user = authService.getUserById(userDetails.getId());
            return ResponseEntity.ok(new AuthDTOs.UserDTO(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/instructor-apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> applyAsInstructor(
            @Valid @RequestBody AuthDTOs.InstructorApplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        try {
            return ResponseEntity.ok(authService.applyAsInstructor(userDetails.getId(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/instructor-application")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyInstructorApplication(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş tələb olunur");
        }
        AuthDTOs.InstructorApplicationDTO dto = authService.getMyInstructorApplication(userDetails.getId());
        return ResponseEntity.ok(dto);
    }
}
