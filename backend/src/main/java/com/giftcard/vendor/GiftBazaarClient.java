package com.giftcard.vendor;

import com.giftcard.model.Vendor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MOCK of Vendor B - GiftBazaar (see assignment's Vendor API Specs).
 * Real integration would call POST https://giftbazaar.mock/api/v2/purchase
 * with header Authorization: Bearer {token}. Here we just simulate it.
 *
 * TODO: tune the stock-out simulation to whatever you want for demoing failover.
 */
@Component
public class GiftBazaarClient implements VendorClient {

    @Override
    public Vendor getVendorName() {
        return Vendor.GIFTBAZAAR;
    }

    @Override
    public VendorOrderResult placeOrder(String vendorProductId, int denomination, String customerEmail, String requestId) {
        // TODO: simulate occasional STOCK_EMPTY to exercise your failover path
        boolean simulateStockEmpty = false;

        if (simulateStockEmpty) {
            return VendorOrderResult.failure("STOCK_EMPTY", "This product is not available at the moment");
        }

        String txnId = "GB-TXN-" + (10000 + new java.util.Random().nextInt(89999));
        String cardNumber = "AMZN-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase()
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String pin = String.valueOf(1000 + new java.util.Random().nextInt(9000));
        String expiry = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        return VendorOrderResult.success(txnId, cardNumber, pin, expiry);
    }
}
