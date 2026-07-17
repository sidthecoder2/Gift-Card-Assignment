package com.giftcard.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class VendorCardDetail {
    private String vendorProductId;
    private String title;
    private String category;
    private String description;
    private String terms;
    private Integer validityDays;
    private Map<Integer, Integer> priceMap;
    private boolean inStock;
}