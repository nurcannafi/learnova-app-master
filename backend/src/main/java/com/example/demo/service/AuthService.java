package com.example.demo.service;

import com.example.demo.dto.AuthDTOs;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.InstructorApplication;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.InstructorApplicationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final InstructorApplicationRepository instructorApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       InstructorApplicationRepository instructorApplicationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.instructorApplicationRepository = instructorApplicationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Bu email artıq qeydiyyatdan keçib: " + cleanEmail);
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Şifrə ən azı 6 simvol olmalıdır");
        }
        if (request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Şifrələr uyğun gəlmir");
        }

        // Never trust client-supplied role — public registration is STUDENT only
        User user = new User(
                request.getFullName().trim(),
                cleanEmail,
                Role.STUDENT,
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateTokenFromUser(
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

        return new AuthDTOs.AuthResponse(token, new AuthDTOs.UserDTO(savedUser));
    }

    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Email və ya şifrə yanlışdır"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email və ya şifrə yanlışdır");
        }

        String token = jwtTokenProvider.generateTokenFromUser(
                user.getId(), user.getEmail(), user.getRole().name());
        return new AuthDTOs.AuthResponse(token, new AuthDTOs.UserDTO(user));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("İstifadəçi tapılmadı: " + userId));
    }

    @Transactional
    public AuthDTOs.InstructorApplicationDTO applyAsInstructor(Long userId, AuthDTOs.InstructorApplyRequest request) {
        User user = getUserById(userId);

        if (user.getRole() == Role.INSTRUCTOR || user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Siz artıq müəllim və ya inzibatçı hesabına sahibsiniz");
        }
        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Yalnız tələbə hesabı müəllim müraciəti edə bilər");
        }
        if (instructorApplicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new IllegalArgumentException("Artıq gözləmədə olan müəllim müraciətiniz var");
        }

        InstructorApplication application = new InstructorApplication(user, request.getBio().trim());
        InstructorApplication saved = instructorApplicationRepository.save(application);
        return new AuthDTOs.InstructorApplicationDTO(saved);
    }

    @Transactional(readOnly = true)
    public AuthDTOs.InstructorApplicationDTO getMyInstructorApplication(Long userId) {
        return instructorApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(AuthDTOs.InstructorApplicationDTO::new)
                .orElse(null);
    }
}
