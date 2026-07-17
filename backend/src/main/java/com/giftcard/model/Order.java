package com.giftcard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Casdoor user id (the JWT 'sub' claim) - who placed this order */
    private String userId;

    @ManyToOne
    @JoinColumn(name = "gift_card_id")
    private GiftCard giftCard;

    private Integer denomination;
    private Integer price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /** Internal only - which vendor actually fulfilled it. Never returned to frontend. */
    @Enumerated(EnumType.STRING)
    private Vendor fulfilledByVendor;

    /** Idempotency key sent to the vendor, also useful for retries */
    private String requestId;

    private String voucherCode;
    private String voucherPin;
    private String expiryDate;
    private String failureMessage;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
