package com.example.demo.service;

import com.example.demo.dto.CertificateDTO;
import com.example.demo.entity.Certificate;
import com.example.demo.repository.CertificateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Transactional(readOnly = true)
    public CertificateDTO getCertificateByCode(String code) {
        Certificate cert = certificateRepository.findByCertificateCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Sertifikat tapılmadı: " + code));
        return toDTO(cert);
    }

    @Transactional(readOnly = true)
    public List<CertificateDTO> getUserCertificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private CertificateDTO toDTO(Certificate c) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(c.getId());
        dto.setCertificateCode(c.getCertificateCode());
        dto.setUserId(c.getUser().getId());
        dto.setStudentName(c.getUser().getFullName());
        dto.setCourseId(c.getCourse().getId());
        dto.setCourseTitle(c.getCourse().getTitle());
        if (c.getCourse().getInstructor() != null) {
            dto.setInstructorName(c.getCourse().getInstructor().getFullName());
        }
        dto.setIssuedAt(c.getIssuedAt());
        return dto;
    }
}
