package com.giftcard.service;

import com.giftcard.model.GiftCard;
import com.giftcard.model.GiftCardVendorOffer;
import com.giftcard.repository.GiftCardRepository;
import com.giftcard.repository.GiftCardVendorOfferRepository;
import com.giftcard.vendor.VendorCardDetail;
import com.giftcard.vendor.VendorCardListItem;
import com.giftcard.vendor.VendorClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VendorSyncService implements ApplicationRunner {

    private final List<VendorClient> vendorClients;
    private final GiftCardRepository giftCardRepository;
    private final GiftCardVendorOfferRepository offerRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (VendorClient client : vendorClients) {
            for (VendorCardListItem listItem : client.listCards()) {
                VendorCardDetail detail = client.getCardDetail(listItem.getVendorProductId());
                GiftCard giftCard = upsertGiftCard(listItem, detail);
                upsertOffers(giftCard, client, listItem.getVendorProductId(), listItem.getPriceMap(),
                        listItem.isInStock());
            }
        }
    }

    private GiftCard upsertGiftCard(VendorCardListItem listItem, VendorCardDetail detail) {
        GiftCard giftCard = giftCardRepository.findByTitle(listItem.getTitle()).orElseGet(GiftCard::new);
        giftCard.setTitle(listItem.getTitle());
        giftCard.setCategory(listItem.getCategory());
        if (giftCard.getImageUrl() == null) {
            String color = listItem.getTitle().contains("Amazon") ? "FF9900" : "2874F0";
            giftCard.setImageUrl("https://placehold.co/400x240/" + color + "/FFFFFF?text="
                    + listItem.getTitle().replace(" ", "+") + "&font=montserrat");
        }
        giftCard.setDescription(detail.getDescription());
        giftCard.setTerms(detail.getTerms());
        giftCard.setValidityDays(detail.getValidityDays());
        return giftCardRepository.save(giftCard);
    }

    private void upsertOffers(GiftCard giftCard, VendorClient client, String vendorProductId,
            Map<Integer, Integer> priceMap, boolean inStock) {
        for (Map.Entry<Integer, Integer> entry : priceMap.entrySet()) {
            GiftCardVendorOffer offer = offerRepository
                    .findByGiftCardIdAndVendorAndDenomination(giftCard.getId(), client.getVendorName(), entry.getKey())
                    .orElseGet(GiftCardVendorOffer::new);

            offer.setGiftCard(giftCard);
            offer.setVendor(client.getVendorName());
            offer.setVendorProductId(vendorProductId);
            offer.setDenomination(entry.getKey());
            offer.setSellingPrice(entry.getValue());
            offer.setInStock(inStock);
            offerRepository.save(offer);
        }
    }
}