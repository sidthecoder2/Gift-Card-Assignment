package com.giftcard.repository;

import com.giftcard.model.GiftCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    List<GiftCard> findByCategory(String category);

    Optional<GiftCard> findByTitle(String title);
}