package com.giftcard.repository;

import com.giftcard.model.GiftCardVendorOffer;
import com.giftcard.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftCardVendorOfferRepository extends JpaRepository<GiftCardVendorOffer, Long> {
    List<GiftCardVendorOffer> findByGiftCardId(Long giftCardId);

    Optional<GiftCardVendorOffer> findByGiftCardIdAndDenomination(Long giftCardId, Integer denomination);

    Optional<GiftCardVendorOffer> findByGiftCardIdAndVendorAndDenomination(Long giftCardId, Vendor vendor,
            Integer denomination);
}