package com.giftcard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * One row per (gift card, vendor, denomination) combination.
 * e.g. Amazon 1000 via QWIKGIFT at 965, and Amazon 1000 via GIFTBAZAAR at 970.
 *
 * This is what lets you:
 *  - merge listings across vendors for the same logical gift card
 *  - compute commission (faceValue - sellingPrice) per vendor
 *  - pick the higher-commission vendor, and fail over to the other on stock-out
 *
 * NEVER expose vendor, vendorProductId directly to the frontend/user.
 */
@Entity
@Table(name = "gift_card_vendor_offers")
@Getter
@Setter
public class GiftCardVendorOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gift_card_id")
    private GiftCard giftCard;

    @Enumerated(EnumType.STRING)
    private Vendor vendor;

    /** The vendor's own identifier for this product, e.g. "QG-1001" or "GB-AZ-001" */
    private String vendorProductId;

    private Integer denomination; // face value, e.g. 1000
    private Integer sellingPrice; // what the vendor charges us, e.g. 965
    private Boolean inStock;

    // Convenience - not persisted
    @Transient
    public int getCommission() {
        return denomination - sellingPrice;
    }
}
