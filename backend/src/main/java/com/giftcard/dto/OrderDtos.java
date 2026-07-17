package com.giftcard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class OrderDtos {

    @Getter
    @Setter
    public static class PlaceOrderRequest {
        @NotNull
        private Long giftCardId;

        @NotNull
        private Integer denomination;

        @Email
        @NotNull
        private String customerEmail;
    }

    @Getter
    @AllArgsConstructor
    public static class OrderResponse {
        private Long orderId;
        private String giftCardTitle;
        private Integer denomination;
        private Integer price;
        private String status;       // PROCESSING / SUCCESS / FAILED / CANCELLED
        private String voucherCode;  // null unless SUCCESS
        private String voucherPin;   // null unless SUCCESS
        private String expiryDate;   // null unless SUCCESS
        private String failureMessage; // null unless FAILED
        private String createdAt;
        // Deliberately no vendor field - internal only
    }
}
