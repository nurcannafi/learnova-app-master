package com.example.demo.controller;

import com.example.demo.dto.CertificateDTO;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getCertificateByCode(@PathVariable String code) {
        try {
            CertificateDTO cert = certificateService.getCertificateByCode(code);
            return ResponseEntity.ok(cert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-certificates")
    public ResponseEntity<List<CertificateDTO>> getMyCertificates(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(certificateService.getUserCertificates(userDetails.getId()));
    }
}
