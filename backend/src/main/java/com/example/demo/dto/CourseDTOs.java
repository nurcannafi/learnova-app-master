package com.example.demo.dto;

import com.example.demo.entity.ContentType;
import com.example.demo.entity.CourseLevel;
import com.example.demo.entity.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CourseDTOs {

    public static class CourseSummaryDTO {
        private Long id;
        private String title;
        private String slug;
        private BigDecimal price;
        private CourseLevel level;
        private CourseStatus status;
        private Long categoryId;
        private String categoryName;
        private Long instructorId;
        private String instructorName;
        private String thumbnailUrl;
        private String description;
        private Double rating;
        private Long totalReviews;
        private Long totalStudents;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public CourseLevel getLevel() { return level; }
        public void setLevel(CourseLevel level) { this.level = level; }
        public CourseStatus getStatus() { return status; }
        public void setStatus(CourseStatus status) { this.status = status; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Long getInstructorId() { return instructorId; }
        public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }
        public String getInstructorName() { return instructorName; }
        public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Long getTotalReviews() { return totalReviews; }
        public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
        public Long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(Long totalStudents) { this.totalStudents = totalStudents; }
    }

    public static class CourseDetailDTO extends CourseSummaryDTO {
        private List<SectionDTO> sections = new ArrayList<>();
        private boolean isEnrolled = false;
        private Integer userProgress = 0;

        public List<SectionDTO> getSections() { return sections; }
        public void setSections(List<SectionDTO> sections) { this.sections = sections; }
        public boolean isEnrolled() { return isEnrolled; }
        public void setEnrolled(boolean enrolled) { isEnrolled = enrolled; }
        public Integer getUserProgress() { return userProgress; }
        public void setUserProgress(Integer userProgress) { this.userProgress = userProgress; }
    }

    public static class SectionDTO {
        private Long id;
        private String title;
        private Integer orderIndex;
        private List<LectureDTO> lectures = new ArrayList<>();

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public List<LectureDTO> getLectures() { return lectures; }
        public void setLectures(List<LectureDTO> lectures) { this.lectures = lectures; }
    }

    public static class LectureDTO {
        private Long id;
        private String title;
        private ContentType contentType;
        private String contentUrl;
        private String textContent;
        private Integer durationMinutes;
        private Integer orderIndex;
        private Boolean isCompleted = false;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public ContentType getContentType() { return contentType; }
        public void setContentType(ContentType contentType) { this.contentType = contentType; }
        public String getContentUrl() { return contentUrl; }
        public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
        public String getTextContent() { return textContent; }
        public void setTextContent(String textContent) { this.textContent = textContent; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public Boolean getIsCompleted() { return isCompleted; }
        public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    }

    public static class CourseCreateRequest {
        @NotBlank(message = "Kursun adı tələb olunur")
        private String title;

        @NotNull(message = "Kateqoriya seçilməlidir")
        private Long categoryId;

        @NotNull(message = "Qiymət daxil edilməlidir")
        private BigDecimal price;

        private CourseLevel level = CourseLevel.BEGINNER;
        private String description;
        private String thumbnailUrl;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public CourseLevel getLevel() { return level; }
        public void setLevel(CourseLevel level) { this.level = level; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    }

    public static class SectionCreateRequest {
        @NotBlank(message = "Bölmə adı tələb olunur")
        private String title;
        private Integer orderIndex = 0;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    }

    public static class LectureCreateRequest {
        @NotBlank(message = "Dərs adı tələb olunur")
        private String title;
        private ContentType contentType = ContentType.VIDEO;
        private String contentUrl;
        private String textContent;
        private Integer durationMinutes = 5;
        private Integer orderIndex = 0;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public ContentType getContentType() { return contentType; }
        public void setContentType(ContentType contentType) { this.contentType = contentType; }
        public String getContentUrl() { return contentUrl; }
        public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
        public String getTextContent() { return textContent; }
        public void setTextContent(String textContent) { this.textContent = textContent; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    }
}
