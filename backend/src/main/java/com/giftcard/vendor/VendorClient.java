package com.giftcard.vendor;

import com.giftcard.model.Vendor;

import java.util.List;

public interface VendorClient {

    Vendor getVendorName();

    /**
     * MOCK of the vendor's "List Gift Cards" endpoint
     * (QwikGift: GET /cards, GiftBazaar: GET /products).
     */
    List<VendorCardListItem> listCards();

    /**
     * MOCK of the vendor's "Get Gift Card Details" endpoint
     * (QwikGift: GET /cards/{id}, GiftBazaar: GET /products/{sku}).
     */
    VendorCardDetail getCardDetail(String vendorProductId);

    /**
     * Attempt to fulfill an order with this vendor.
     */
    VendorOrderResult placeOrder(String vendorProductId, int denomination, String customerEmail, String requestId);
}