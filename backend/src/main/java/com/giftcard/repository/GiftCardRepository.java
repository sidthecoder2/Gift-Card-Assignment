package com.giftcard.repository;

import com.giftcard.model.GiftCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    List<GiftCard> findByCategory(String category);
}
