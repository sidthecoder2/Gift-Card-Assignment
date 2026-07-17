package com.giftcard.service;

import com.giftcard.model.GiftCardVendorOffer;
import com.giftcard.model.Vendor;
import com.giftcard.repository.GiftCardVendorOfferRepository;
import com.giftcard.vendor.GiftBazaarClient;
import com.giftcard.vendor.QwikGiftClient;
import com.giftcard.vendor.VendorClient;
import com.giftcard.vendor.VendorOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VendorAggregatorService {

    private final GiftCardVendorOfferRepository offerRepository;
    private final QwikGiftClient qwikGiftClient;
    private final GiftBazaarClient giftBazaarClient;

    private Map<Vendor, VendorClient> clientsByVendor() {
        Map<Vendor, VendorClient> map = new EnumMap<>(Vendor.class);
        map.put(Vendor.QWIKGIFT, qwikGiftClient);
        map.put(Vendor.GIFTBAZAAR, giftBazaarClient);
        return map;
    }

    public static class FulfillmentOutcome {
        public boolean success;
        public Vendor fulfilledBy;
        public VendorOrderResult result;
        public Integer sellingPrice;
    }

    /**
     * Order of operations:
     * 1. Load all vendor offers for this gift card + denomination.
     * 2. Sort by commission (denomination - sellingPrice) descending -> preferred
     * vendor first.
     * 3. Try the preferred (in-stock) vendor's mock API.
     * 4. If it fails/out-of-stock, try the next vendor in the list.
     * 5. If all fail, return failure.
     */
    public FulfillmentOutcome fulfill(Long giftCardId, int denomination, String customerEmail, String requestId) {
        List<GiftCardVendorOffer> offers = offerRepository.findByGiftCardId(giftCardId);

        List<GiftCardVendorOffer> candidateOffers = offers.stream()
                .filter(o -> o.getDenomination().equals(denomination))
                .filter(o -> Boolean.TRUE.equals(o.getInStock()))
                .sorted(Comparator.comparingInt(GiftCardVendorOffer::getCommission).reversed())
                .toList();

        Map<Vendor, VendorClient> clients = clientsByVendor();

        FulfillmentOutcome outcome = new FulfillmentOutcome();

        for (GiftCardVendorOffer offer : candidateOffers) {
            VendorClient client = clients.get(offer.getVendor());
            VendorOrderResult result = client.placeOrder(
                    offer.getVendorProductId(), denomination, customerEmail, requestId);

            if (result.isSuccess()) {
                outcome.success = true;
                outcome.fulfilledBy = offer.getVendor();
                outcome.sellingPrice = offer.getSellingPrice();
                outcome.result = result;
                return outcome;
            }
            // else: fall through and try the next vendor (failover)
            outcome.result = result; // keep the last failure message around
        }

        outcome.success = false;
        return outcome;
    }
}
