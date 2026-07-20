# Understanding Document — Gift Card Platform

## My Understanding of the Problem

The goal was to build a platform where a user can log in, browse gift cards, and place an order — but behind the scenes, the actual gift card could come from either of two vendors (QwikGift or GiftBazaar), each with their own API shape, pricing, and stock status. The core challenge wasn't really the CRUD (list/order/history) — it was the **vendor arbitration layer**: showing the user one clean merged catalog while internally deciding, per order, which vendor to actually fulfill through, based on commission, with a fallback if the preferred vendor fails.

Three explicit constraints shaped my design:

1. The user should never know which vendor fulfilled their order.
2. If a gift card is available from both vendors, prefer whichever gives the higher commission (face value − selling price).
3. If the preferred vendor is out of stock or fails, silently retry the other vendor before failing the order.

Authentication had to go through Casdoor specifically (not any auth provider of my choice), which was the one piece of this stack I hadn't used before.

## Architecture

```
┌─────────────┐      OAuth redirect       ┌──────────────┐
│   React     │ ─────────────────────────▶│   Casdoor    │
│  Frontend   │◀───────── code ───────────│ (auth server)│
└──────┬──────┘                            └──────────────┘
       │  POST /api/auth/token (code)             ▲
       ▼                                            │ validates
┌─────────────────────────────────────────┐        │ credentials
│         Spring Boot Backend             │        │
│  ┌────────────────────────────────┐     │        │
│  │ AuthController                 │─────┼────────┘
│  │ (proxies code→token exchange)  │     │  server-side call,
│  └────────────────────────────────┘     │  keeps client secret
│  ┌────────────────────────────────┐     │  out of the browser
│  │ SecurityConfig                 │     │
│  │ (validates JWT on every        │     │
│  │  other /api/** request)        │     │
│  └────────────────────────────────┘     │
│  ┌────────────────────────────────┐     │
│  │ GiftCardController/Service     │     │
│  │ - merges vendor offers         │     │
│  │ - hides vendor identity        │     │
│  └────────────────────────────────┘     │
│  ┌────────────────────────────────┐     │
│  │ OrderController/Service        │     │
│  └──────────────┬─────────────────┘     │
│  ┌──────────────▼─────────────────┐     │
│  │ VendorAggregatorService        │     │
│  │ - picks highest-commission     │     │
│  │   in-stock vendor              │     │
│  │ - fails over to the other on   │     │
│  │   stock-out/failure            │     │
│  └──────────────┬─────────────────┘     │
│         ┌────────┴────────┐              │
│  ┌──────▼──────┐   ┌──────▼───────┐      │
│  │ QwikGift    │   │ GiftBazaar   │      │
│  │ mock client │   │ mock client  │      │
│  └─────────────┘   └──────────────┘      │
│  ┌────────────────────────────────┐     │
│  │ VendorSyncService (startup)     │     │
│  │ calls both mock clients'        │     │
│  │ listCards()/getCardDetail(),    │     │
│  │ upserts merged result into DB   │     │
│  └────────────────────────────────┘     │
└──────────────────┬───────────────────────┘
                    ▼
             PostgreSQL (gift_cards,
             gift_card_vendor_offers, orders)
```

**Layers, and why they're split this way:**

- **Frontend (React)** — five screens (Login, Home, Detail, Confirmation, History), each doing its own data fetching with loading/error states. Talks to the backend only via a thin `api/client.js` wrapper that attaches the Bearer token to every call.
- **AuthController** — the one intentionally _unauthenticated_ endpoint. It exists purely to keep the Casdoor client secret server-side: the frontend only ever handles the short-lived authorization `code`, never the secret.
- **SecurityConfig** — every other endpoint requires a valid Casdoor-issued JWT, verified against Casdoor's issuer/JWKS. This is what makes "unauthenticated requests should be rejected" actually true rather than just intended.
- **GiftCardService / OrderService** — standard controller-service-repository layering. These two don't know anything about vendors directly; they just read/write `GiftCard`, `GiftCardVendorOffer`, and `Order` rows.
- **VendorAggregatorService** — the one piece of real business logic in the system. Given a gift card + denomination, it sorts the available offers by commission (highest first) and tries each vendor's mock client in that order, moving to the next on failure. Everything above this layer is unaware of which vendor actually fulfilled the order.
- **VendorSyncService** — runs once at startup. It calls each mock vendor client's `listCards()`/`getCardDetail()` (simulating the vendors' real List/Detail HTTP endpoints from the spec) and upserts the results into the database. This is what the rest of the system actually reads from — so the vendor mocks aren't just decorative, they're the real source of the catalog data.
- **Database (PostgreSQL)** — three tables: `gift_cards` (user-facing catalog), `gift_card_vendor_offers` (internal, per-vendor pricing/stock — never serialized to the frontend), `orders` (one row per placed order, including an internal-only `fulfilledByVendor` column).

## My Approach

**Backend-first, then auth, then frontend.** I chose this order deliberately: the vendor merge/commission/failover logic is the part of the assignment with actual business logic worth getting right, so I wanted that solid and testable (via Postman) before layering UI on top of it. Casdoor came second because it was the highest-risk unknown — I wanted to validate it end-to-end early rather than discover integration problems on day 4.

**Stack:** Java + Spring Boot for the backend, since it's what I already work in professionally and could move fastest and most confidently in. React + Vite for the frontend. PostgreSQL for the app's own data, since the vendor-offer-merging query benefits from standard relational joins/grouping. Casdoor via Docker as required.

**Key design decision — separating vendor data from gift card data.** I split the schema into `gift_cards` (what the user sees) and `gift_card_vendor_offers` (one row per vendor+denomination combination, internal only). This is what makes the merge logic clean: the aggregator just queries all offers for a gift card, sorts by commission, and tries vendors in order. It also means vendor identity never has to be filtered out of API responses — it's structurally never in the same table as what gets serialized to the frontend.

**Key design decision — mocking vendors as if they were real HTTP integrations.** Rather than hand-writing merged catalog data directly into the database, I built `QwikGiftClient` and `GiftBazaarClient` as mock implementations of the vendors' actual List/Detail/Fulfill contracts from the spec, and wrote a `VendorSyncService` that calls their list/detail methods at startup and populates the database from those responses. This was a deliberate choice to make the mocking genuine — if I were to swap in real HTTP calls later, only the two client classes would need to change, nothing else in the system.

**Key design decision — OAuth token exchange happens server-side.** Casdoor's authorization code flow hands back a `code` that needs exchanging for a token. I proxy that exchange through my own backend (`AuthController`) rather than doing it directly in the browser, so the Casdoor client secret never ships in frontend JavaScript.

## Challenges I Ran Into (and What I Learned)

- **A Windows-JVM timezone bug** (`Asia/Calcutta` vs `Asia/Kolkata`) broke the Postgres JDBC connection outright until I pinned the JVM's default timezone explicitly in code at startup — a good reminder that "works on my machine" assumptions about JVM defaults aren't safe across OSes.
- **Port collisions** with pre-existing local Postgres installs (from pgAdmin) taught me to never assume default ports are free, and to make that configurable/documented rather than hardcoded.
- **Casdoor's organization/application isolation model** — I initially didn't understand why my admin login stopped working after linking my Application to a custom organization instead of `built-in`. Digging into it clarified that this isolation is intentional Casdoor behavior, not a bug, and shaped how I now think about multi-tenant auth systems generally.
- **Data.sql vs. application-driven seeding ordering** — I hit a real Spring Boot gotcha where `data.sql` ran before Hibernate had created the schema. Fixing it properly (`defer-datasource-initialization`) was more instructive than just working around it.

## What I'd Do Differently With More Time

- Make vendor failure simulation randomized/configurable rather than a hardcoded boolean flag, so failover could be demonstrated without a manual code edit + restart.
- Add asynchronous order fulfillment (a queue + webhook-style callback) so the `PROCESSING` status — and the cancel flow — would be meaningfully reachable, rather than resolving synchronously within the same request.
- Move the Casdoor client secret out of `application.yml` and into environment variables/a secrets store, even for local dev.

## Status

All parts of the brief are implemented and manually verified: Casdoor auth (signup/login, protected APIs), all 6 required backend endpoints, vendor merge/commission/failover logic (tested both the "preferred vendor succeeds" and "preferred vendor fails, other vendor picks up" paths), and all 5 frontend screens with loading/error states. Full details on setup and architecture are in `README.md`.
