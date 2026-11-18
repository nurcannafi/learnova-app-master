package com.example.demo.controller;

import com.example.demo.dto.CouponDTOs;
import com.example.demo.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = "*")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponDTOs.CouponValidateResponse> validateCoupon(
            @RequestBody CouponDTOs.CouponValidateRequest request) {
        return ResponseEntity.ok(couponService.validateCoupon(request.getCode(), request.getOriginalPrice()));
    }
}
