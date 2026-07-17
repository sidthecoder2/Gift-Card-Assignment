package com.giftcard.vendor;

import com.giftcard.model.Vendor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class QwikGiftClient implements VendorClient {

    @Override
    public Vendor getVendorName() {
        return Vendor.QWIKGIFT;
    }

    @Override
    public List<VendorCardListItem> listCards() {
        Map<Integer, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(500, 485);
        priceMap.put(1000, 965);
        priceMap.put(2000, 1920);
        priceMap.put(5000, 4800);

        return List.of(new VendorCardListItem("QG-1001", "Amazon Gift Card", "shopping", priceMap, true));
    }

    @Override
    public VendorCardDetail getCardDetail(String vendorProductId) {
        Map<Integer, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(500, 485);
        priceMap.put(1000, 965);
        priceMap.put(2000, 1920);
        priceMap.put(5000, 4800);

        return new VendorCardDetail(
                vendorProductId,
                "Amazon Gift Card",
                "shopping",
                "Redeemable on Amazon.in for all products",
                "Valid for 12 months from date of issue. Non-refundable.",
                365,
                priceMap,
                true);
    }

    @Override
    public VendorOrderResult placeOrder(String vendorProductId, int denomination, String customerEmail,
            String requestId) {
        boolean simulateOutOfStock = false;

        if (simulateOutOfStock) {
            return VendorOrderResult.failure("OUT_OF_STOCK", "Requested denomination is currently unavailable");
        }

        String orderId = "QG-ORD-" + (10000 + new java.util.Random().nextInt(89999));
        String voucherCode = "AMZN-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase()
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String pin = String.valueOf(1000 + new java.util.Random().nextInt(9000));
        String expiry = LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_DATE);

        return VendorOrderResult.success(orderId, voucherCode, pin, expiry);
    }
}