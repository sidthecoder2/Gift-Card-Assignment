package com.giftcard.service;

import com.giftcard.dto.GiftCardResponse;
import com.giftcard.model.GiftCard;
import com.giftcard.model.GiftCardVendorOffer;
import com.giftcard.repository.GiftCardRepository;
import com.giftcard.repository.GiftCardVendorOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final GiftCardVendorOfferRepository offerRepository;

    public List<GiftCardResponse> listGiftCards(String category) {
        List<GiftCard> cards = (category == null || category.isBlank())
                ? giftCardRepository.findAll()
                : giftCardRepository.findByCategory(category);

        return cards.stream()
                .map(card -> toResponse(card, false))
                .toList();
    }

    public GiftCardResponse getDetail(Long id) {
        GiftCard card = giftCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + id));
        return toResponse(card, true);
    }

    private GiftCardResponse toResponse(GiftCard card, boolean includeFullDetail) {
        List<GiftCardVendorOffer> offers = offerRepository.findByGiftCardId(card.getId());

        // Merge: for each denomination, across all in-stock vendor offers, show ONE price.
        // TODO: decide + document in README what "price shown to user" actually means
        // (e.g. cheapest vendor's selling price, or a markup on top of it). Using the
        // lowest selling price among in-stock vendors as a placeholder here.
        Map<Integer, Integer> bestPricePerDenomination = offers.stream()
                .filter(o -> Boolean.TRUE.equals(o.getInStock()))
                .collect(Collectors.groupingBy(
                        GiftCardVendorOffer::getDenomination,
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparingInt(GiftCardVendorOffer::getSellingPrice)),
                                opt -> opt.map(GiftCardVendorOffer::getSellingPrice).orElse(null)
                        )
                ));

        List<GiftCardResponse.DenominationPrice> denominations = bestPricePerDenomination.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new GiftCardResponse.DenominationPrice(e.getKey(), e.getValue()))
                .toList();

        return new GiftCardResponse(
                card.getId(),
                card.getTitle(),
                card.getCategory(),
                card.getImageUrl(),
                includeFullDetail ? card.getDescription() : null,
                includeFullDetail ? card.getTerms() : null,
                card.getValidityDays(),
                denominations
        );
    }
}
