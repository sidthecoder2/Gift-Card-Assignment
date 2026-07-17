package com.giftcard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A gift card as the USER sees it. Vendor-specific details (which vendor,
 * their SKU, their price) live in GiftCardVendorOffer - never exposed via API.
 */
@Entity
@Table(name = "gift_cards")
@Getter
@Setter
public class GiftCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String category;
    private String imageUrl;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String terms;

    private Integer validityDays;

    @OneToMany(mappedBy = "giftCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiftCardVendorOffer> vendorOffers;
}
