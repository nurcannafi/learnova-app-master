package com.example.demo.service;

import com.example.demo.dto.CouponDTOs;
import com.example.demo.entity.Coupon;
import com.example.demo.entity.DiscountType;
import com.example.demo.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public CouponDTOs.CouponValidateResponse validateCoupon(String code, BigDecimal originalPrice) {
        CouponDTOs.CouponValidateResponse resp = new CouponDTOs.CouponValidateResponse();
        resp.setOriginalPrice(originalPrice);
        resp.setCode(code);

        if (code == null || code.trim().isEmpty()) {
            resp.setValid(false);
            resp.setMessage("Kupon kodu daxil edilməyib");
            resp.setFinalPrice(originalPrice);
            return resp;
        }

        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(code.trim());
        if (couponOpt.isEmpty()) {
            resp.setValid(false);
            resp.setMessage("Kupon tapılmadı və ya etibarsızdır");
            resp.setFinalPrice(originalPrice);
            return resp;
        }

        Coupon coupon = couponOpt.get();
        if (!coupon.isValid()) {
            resp.setValid(false);
            resp.setMessage("Kuponun istifadə müddəti bitib");
            resp.setFinalPrice(originalPrice);
            return resp;
        }

        BigDecimal discountAmount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = originalPrice.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        if (discountAmount.compareTo(originalPrice) > 0) {
            discountAmount = originalPrice;
        }

        BigDecimal finalPrice = originalPrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        resp.setValid(true);
        resp.setMessage("Kupon uğurla tətbiq edildi!");
        resp.setDiscountType(coupon.getDiscountType());
        resp.setDiscountValue(coupon.getDiscountValue());
        resp.setDiscountAmount(discountAmount);
        resp.setFinalPrice(finalPrice);
        return resp;
    }

    public Coupon getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        return couponRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
    }
}
