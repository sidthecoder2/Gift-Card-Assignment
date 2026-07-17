DELETE FROM gift_card_vendor_offers;
DELETE FROM orders;
DELETE FROM gift_cards;

ALTER SEQUENCE IF EXISTS gift_cards_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS gift_card_vendor_offers_id_seq RESTART WITH 1;