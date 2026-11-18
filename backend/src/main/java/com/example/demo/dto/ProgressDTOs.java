package com.example.demo.dto;

public class ProgressDTOs {

    public static class LectureProgressRequest {
        private Long lectureId;
        private Boolean isCompleted;

        public Long getLectureId() { return lectureId; }
        public void setLectureId(Long lectureId) { this.lectureId = lectureId; }
        public Boolean getIsCompleted() { return isCompleted; }
        public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    }

    public static class CourseProgressResponse {
        private Long courseId;
        private String courseTitle;
        private long totalLectures;
        private long completedLectures;
        private int progressPercentage;
        private boolean isCompleted;
        private String certificateCode;

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        public long getTotalLectures() { return totalLectures; }
        public void setTotalLectures(long totalLectures) { this.totalLectures = totalLectures; }
        public long getCompletedLectures() { return completedLectures; }
        public void setCompletedLectures(long completedLectures) { this.completedLectures = completedLectures; }
        public int getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        public String getCertificateCode() { return certificateCode; }
        public void setCertificateCode(String certificateCode) { this.certificateCode = certificateCode; }
    }
}
