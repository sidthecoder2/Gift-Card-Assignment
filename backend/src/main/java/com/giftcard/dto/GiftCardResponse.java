package com.giftcard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GiftCardResponse {
    private Long id;
    private String title;
    private String category;
    private String imageUrl;
    private String description; // null for listing view, populated for detail view
    private String terms;       // null for listing view, populated for detail view
    private Integer validityDays;
    private List<DenominationPrice> denominations;

    @Getter
    @AllArgsConstructor
    public static class DenominationPrice {
        private Integer denomination;
        private Integer price; // best (cheapest to us / doesn't matter to user) merged price
    }
}
