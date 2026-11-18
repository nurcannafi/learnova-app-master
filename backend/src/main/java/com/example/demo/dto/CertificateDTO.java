package com.example.demo.dto;

import java.time.LocalDateTime;

public class CertificateDTO {
    private Long id;
    private String certificateCode;
    private Long userId;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private String instructorName;
    private LocalDateTime issuedAt;

    public CertificateDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCertificateCode() { return certificateCode; }
    public void setCertificateCode(String certificateCode) { this.certificateCode = certificateCode; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
