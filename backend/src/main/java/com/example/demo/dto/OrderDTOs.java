package com.example.demo.dto;

import com.example.demo.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDTOs {

    public static class CheckoutRequest {
        private List<Long> courseIds = new ArrayList<>();
        private String couponCode;

        public List<Long> getCourseIds() { return courseIds; }
        public void setCourseIds(List<Long> courseIds) { this.courseIds = courseIds; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    }

    public static class OrderResponse {
        private Long id;
        private String code;
        private BigDecimal totalPrice;
        private OrderStatus status;
        private String couponCode;
        private LocalDateTime createdAt;
        private List<OrderItemDTO> items = new ArrayList<>();

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public List<OrderItemDTO> getItems() { return items; }
        public void setItems(List<OrderItemDTO> items) { this.items = items; }
    }

    public static class OrderItemDTO {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private String courseSlug;
        private String thumbnailUrl;
        private BigDecimal price;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        public String getCourseSlug() { return courseSlug; }
        public void setCourseSlug(String courseSlug) { this.courseSlug = courseSlug; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}
