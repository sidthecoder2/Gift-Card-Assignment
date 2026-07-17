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
public class GiftBazaarClient implements VendorClient {

    @Override
    public Vendor getVendorName() {
        return Vendor.GIFTBAZAAR;
    }

    @Override
    public List<VendorCardListItem> listCards() {
        Map<Integer, Integer> amazonPrices = new LinkedHashMap<>();
        amazonPrices.put(500, 490);
        amazonPrices.put(1000, 970);
        amazonPrices.put(2000, 1940);
        amazonPrices.put(5000, 4850);

        Map<Integer, Integer> flipkartPrices = new LinkedHashMap<>();
        flipkartPrices.put(500, 480);
        flipkartPrices.put(1000, 955);

        return List.of(
                new VendorCardListItem("GB-AZ-001", "Amazon Gift Card", "shopping", amazonPrices, true),
                new VendorCardListItem("GB-FK-002", "Flipkart Gift Card", "shopping", flipkartPrices, true));
    }

    @Override
    public VendorCardDetail getCardDetail(String vendorProductId) {
        if ("GB-FK-002".equals(vendorProductId)) {
            Map<Integer, Integer> priceMap = new LinkedHashMap<>();
            priceMap.put(500, 480);
            priceMap.put(1000, 955);
            return new VendorCardDetail(
                    vendorProductId, "Flipkart Gift Card", "shopping",
                    "Redeemable on Flipkart for all products",
                    "Valid for 12 months from date of issue. Non-refundable.",
                    365, priceMap, true);
        }

        Map<Integer, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(500, 490);
        priceMap.put(1000, 970);
        priceMap.put(2000, 1940);
        priceMap.put(5000, 4850);

        return new VendorCardDetail(
                vendorProductId, "Amazon Gift Card", "shopping",
                "Use on Amazon.in across all categories",
                "Non-refundable. Cannot be exchanged for cash. Valid 1 year.",
                365, priceMap, true);
    }

    @Override
    public VendorOrderResult placeOrder(String vendorProductId, int denomination, String customerEmail,
            String requestId) {
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