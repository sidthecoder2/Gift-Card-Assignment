DELETE FROM gift_card_vendor_offers;
DELETE FROM orders;
DELETE FROM gift_cards;

ALTER SEQUENCE IF EXISTS gift_cards_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS gift_card_vendor_offers_id_seq RESTART WITH 1;

INSERT INTO gift_cards (id, title, category, image_url, description, terms, validity_days) VALUES
(1, 'Amazon Gift Card', 'shopping', 'https://placehold.co/400x240/FF9900/FFFFFF?text=Amazon+Gift+Card&font=montserrat',
 'Redeemable on Amazon.in for all products', 'Valid for 12 months from date of issue. Non-refundable.', 365),
(2, 'Flipkart Gift Card', 'shopping', 'https://placehold.co/400x240/2874F0/FFFFFF?text=Flipkart+Gift+Card&font=montserrat',
 'Redeemable on Flipkart for all products', 'Valid for 12 months from date of issue. Non-refundable.', 365);

INSERT INTO gift_card_vendor_offers (gift_card_id, vendor, vendor_product_id, denomination, selling_price, in_stock) VALUES
(1, 'QWIKGIFT', 'QG-1001', 500, 485, true),
(1, 'QWIKGIFT', 'QG-1001', 1000, 965, true),
(1, 'QWIKGIFT', 'QG-1001', 2000, 1920, true),
(1, 'GIFTBAZAAR', 'GB-AZ-001', 500, 490, true),
(1, 'GIFTBAZAAR', 'GB-AZ-001', 1000, 970, true),
(1, 'GIFTBAZAAR', 'GB-AZ-001', 2000, 1940, true);

INSERT INTO gift_card_vendor_offers (gift_card_id, vendor, vendor_product_id, denomination, selling_price, in_stock) VALUES
(2, 'GIFTBAZAAR', 'GB-FK-002', 500, 480, true),
(2, 'GIFTBAZAAR', 'GB-FK-002', 1000, 955, true);