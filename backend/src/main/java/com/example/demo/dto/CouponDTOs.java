package com.example.demo.dto;

import com.example.demo.entity.DiscountType;
import java.math.BigDecimal;

public class CouponDTOs {

    public static class CouponValidateRequest {
        private String code;
        private BigDecimal originalPrice;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    }

    public static class CouponValidateResponse {
        private boolean valid;
        private String message;
        private String code;
        private BigDecimal originalPrice;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private BigDecimal discountAmount;
        private BigDecimal finalPrice;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public DiscountType getDiscountType() { return discountType; }
        public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    }
}
