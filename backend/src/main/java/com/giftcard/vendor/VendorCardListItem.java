package com.giftcard.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class VendorCardListItem {
    private String vendorProductId; // card_id / sku
    private String title;
    private String category;
    private Map<Integer, Integer> priceMap; // denomination -> vendor's selling price
    private boolean inStock;
}