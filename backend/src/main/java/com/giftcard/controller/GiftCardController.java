package com.giftcard.controller;

import com.giftcard.dto.GiftCardResponse;
import com.giftcard.service.GiftCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gift-cards")
@RequiredArgsConstructor
public class GiftCardController {

    private final GiftCardService giftCardService;

    // GET /api/gift-cards?category=shopping
    @GetMapping
    public List<GiftCardResponse> list(@RequestParam(required = false) String category) {
        return giftCardService.listGiftCards(category);
    }

    // GET /api/gift-cards/{id}
    @GetMapping("/{id}")
    public GiftCardResponse detail(@PathVariable Long id) {
        return giftCardService.getDetail(id);
    }
}
