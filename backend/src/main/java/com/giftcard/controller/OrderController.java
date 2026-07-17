package com.giftcard.controller;

import com.giftcard.dto.OrderDtos.OrderResponse;
import com.giftcard.dto.OrderDtos.PlaceOrderRequest;
import com.giftcard.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders
    @PostMapping
    public OrderResponse placeOrder(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PlaceOrderRequest request) {
        String userId = jwt.getSubject(); // Casdoor user id from the validated token
        return orderService.placeOrder(userId, request);
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public OrderResponse getStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return orderService.getOrderStatus(jwt.getSubject(), id);
    }

    // POST /api/orders/{id}/cancel
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return orderService.cancelOrder(jwt.getSubject(), id);
    }

    // GET /api/orders  (order history for the logged-in user)
    @GetMapping
    public List<OrderResponse> history(@AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrderHistory(jwt.getSubject());
    }
}
