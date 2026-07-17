package com.giftcard.vendor;

import com.giftcard.model.Vendor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MOCK of Vendor A - QwikGift (see assignment's Vendor API Specs).
 * Real integration would call POST https://api.qwikgift.mock/v1/orders
 * with header X-QG-Key. Here we just simulate it.
 *
 * TODO: tune the stock-out simulation to whatever you want for demoing
 * failover.
 */
@Component
public class QwikGiftClient implements VendorClient {

    @Override
    public Vendor getVendorName() {
        return Vendor.QWIKGIFT;
    }

    @Override
    public VendorOrderResult placeOrder(String vendorProductId, int denomination, String customerEmail,
            String requestId) {
        // TODO: simulate occasional OUT_OF_STOCK to exercise your failover path
        boolean simulateOutOfStock = false; // was false

        if (simulateOutOfStock) {
            return VendorOrderResult.failure("OUT_OF_STOCK", "Requested denomination is currently    unavailable");
        }

        String orderId = "QG-ORD-" + (10000 + new java.util.Random().nextInt(89999));
        String voucherCode = "AMZN-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase()
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String pin = String.valueOf(1000 + new java.util.Random().nextInt(9000));
        String expiry = LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_DATE);

        return VendorOrderResult.success(orderId, voucherCode, pin, expiry);
    }
}
