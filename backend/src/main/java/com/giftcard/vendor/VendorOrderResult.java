package com.giftcard.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VendorOrderResult {
    private final boolean success;
    private final String vendorOrderId; // order_id / transaction_id
    private final String voucherCode;
    private final String pin;
    private final String expiryDate;
    private final String errorCode;
    private final String message;

    public static VendorOrderResult success(String vendorOrderId, String voucherCode, String pin, String expiryDate) {
        return new VendorOrderResult(true, vendorOrderId, voucherCode, pin, expiryDate, null, null);
    }

    public static VendorOrderResult failure(String errorCode, String message) {
        return new VendorOrderResult(false, null, null, null, null, errorCode, message);
    }
}
