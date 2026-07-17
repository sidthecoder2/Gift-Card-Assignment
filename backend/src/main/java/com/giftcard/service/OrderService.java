package com.giftcard.service;

import com.giftcard.dto.OrderDtos.OrderResponse;
import com.giftcard.dto.OrderDtos.PlaceOrderRequest;
import com.giftcard.model.GiftCard;
import com.giftcard.model.Order;
import com.giftcard.model.OrderStatus;
import com.giftcard.repository.GiftCardRepository;
import com.giftcard.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final GiftCardRepository giftCardRepository;
    private final VendorAggregatorService vendorAggregatorService;

    public OrderResponse placeOrder(String userId, PlaceOrderRequest request) {
        GiftCard giftCard = giftCardRepository.findById(request.getGiftCardId())
                .orElseThrow(() -> new IllegalArgumentException("Gift card not found"));

        String requestId = UUID.randomUUID().toString();

        Order order = new Order();
        order.setUserId(userId);
        order.setGiftCard(giftCard);
        order.setDenomination(request.getDenomination());
        order.setRequestId(requestId);
        order.setStatus(OrderStatus.PROCESSING);
        order = orderRepository.save(order);

        VendorAggregatorService.FulfillmentOutcome outcome = vendorAggregatorService.fulfill(
                giftCard.getId(), request.getDenomination(), request.getCustomerEmail(), requestId);

        if (outcome.success) {
            order.setStatus(OrderStatus.SUCCESS);
            order.setFulfilledByVendor(outcome.fulfilledBy); // internal only, never returned to user
            order.setVoucherCode(outcome.result.getVoucherCode());
            order.setVoucherPin(outcome.result.getPin());
            order.setExpiryDate(outcome.result.getExpiryDate());
            order.setPrice(outcome.sellingPrice);
        } else {
            order.setStatus(OrderStatus.FAILED);
            order.setFailureMessage(outcome.result != null ? outcome.result.getMessage() : "All vendors failed");
        }

        order = orderRepository.save(order);
        System.out.println("Fulfilled by: " + outcome.fulfilledBy);
        return toResponse(order);
    }

    public OrderResponse getOrderStatus(String userId, Long orderId) {
        Order order = requireOwnedOrder(userId, orderId);
        return toResponse(order);
    }

    public OrderResponse cancelOrder(String userId, Long orderId) {
        Order order = requireOwnedOrder(userId, orderId);
        // TODO: decide business rule - can you cancel a SUCCESS order? Probably not.
        // Likely only PROCESSING orders should be cancellable.
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        return toResponse(order);
    }

    public List<OrderResponse> getOrderHistory(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Order requireOwnedOrder(String userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("Order does not belong to current user");
        }
        return order;
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getGiftCard().getTitle(),
                order.getDenomination(),
                order.getPrice(),
                order.getStatus().name(),
                order.getVoucherCode(),
                order.getVoucherPin(),
                order.getExpiryDate(),
                order.getFailureMessage(),
                order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
    }
}
