package com.giftcard.vendor;

import com.giftcard.model.Vendor;

public interface VendorClient {

    Vendor getVendorName();

    /**
     * Attempt to fulfill an order with this vendor.
     * TODO: replace the hardcoded mock logic inside each implementation with
     * whatever simulated behaviour you want (e.g. randomly fail 10% of the
     * time to test your failover logic).
     */
    VendorOrderResult placeOrder(String vendorProductId, int denomination, String customerEmail, String requestId);
}
