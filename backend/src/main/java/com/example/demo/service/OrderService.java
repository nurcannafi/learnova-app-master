package com.example.demo.service;

import com.example.demo.dto.CouponDTOs;
import com.example.demo.dto.OrderDTOs;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final EnrollmentService enrollmentService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CourseRepository courseRepository,
                        UserRepository userRepository,
                        CouponService couponService,
                        EnrollmentService enrollmentService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.couponService = couponService;
        this.enrollmentService = enrollmentService;
    }

    @Transactional
    public OrderDTOs.OrderResponse checkout(Long userId, OrderDTOs.CheckoutRequest request) {
        if (request.getCourseIds() == null || request.getCourseIds().isEmpty()) {
            throw new IllegalArgumentException("Səbətdə kurs yoxdur");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("İstifadəçi tapılmadı"));

        List<Course> courses = courseRepository.findAllById(request.getCourseIds());
        if (courses.isEmpty()) {
            throw new IllegalArgumentException("Seçilmiş kurslar tapılmadı");
        }

        BigDecimal originalTotal = BigDecimal.ZERO;
        for (Course c : courses) {
            originalTotal = originalTotal.add(c.getPrice());
        }

        Coupon coupon = null;
        BigDecimal finalTotal = originalTotal;

        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            CouponDTOs.CouponValidateResponse val = couponService.validateCoupon(request.getCouponCode(), originalTotal);
            if (val.isValid()) {
                coupon = couponService.getCouponByCode(request.getCouponCode());
                finalTotal = val.getFinalPrice();
            }
        }

        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(user, orderCode, finalTotal, OrderStatus.COMPLETED, coupon);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        for (Course c : courses) {
            OrderItem item = new OrderItem(savedOrder, c, c.getPrice());
            items.add(orderItemRepository.save(item));
            // Enroll user immediately!
            enrollmentService.enroll(userId, c.getId());
        }
        savedOrder.setItems(items);

        return toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDTOs.OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toOrderResponse).collect(Collectors.toList());
    }

    private OrderDTOs.OrderResponse toOrderResponse(Order o) {
        OrderDTOs.OrderResponse resp = new OrderDTOs.OrderResponse();
        resp.setId(o.getId());
        resp.setCode(o.getCode());
        resp.setTotalPrice(o.getTotalPrice());
        resp.setStatus(o.getStatus());
        resp.setCreatedAt(o.getCreatedAt());
        if (o.getCoupon() != null) {
            resp.setCouponCode(o.getCoupon().getCode());
        }

        List<OrderDTOs.OrderItemDTO> itemDTOs = o.getItems().stream().map(item -> {
            OrderDTOs.OrderItemDTO iDto = new OrderDTOs.OrderItemDTO();
            iDto.setId(item.getId());
            iDto.setCourseId(item.getCourse().getId());
            iDto.setCourseTitle(item.getCourse().getTitle());
            iDto.setCourseSlug(item.getCourse().getSlug());
            iDto.setThumbnailUrl(item.getCourse().getThumbnailUrl());
            iDto.setPrice(item.getPrice());
            return iDto;
        }).collect(Collectors.toList());
        resp.setItems(itemDTOs);

        return resp;
    }
}
