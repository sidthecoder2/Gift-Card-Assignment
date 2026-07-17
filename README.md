# Gift Card Platform

A full-stack gift card purchase platform with Casdoor-based authentication, a Spring Boot backend that merges and arbitrates between two mock vendors, and a React frontend.

## Tech Stack

- **Auth:** Casdoor (self-hosted, Docker), OAuth2/OIDC authorization code flow
- **Backend:** Java 17, Spring Boot 3.3, Spring Security (OAuth2 Resource Server), Spring Data JPA
- **Database:** PostgreSQL (application data), MySQL (Casdoor's internal store)
- **Frontend:** React + Vite, React Router
- **Containerization:** Docker Compose for Casdoor + both databases

---

## How to Set Up and Run

### Prerequisites

- Docker Desktop
- Java 17+ and Maven (or an IDE with Java support, e.g. Cursor/VS Code with the Java extension pack)
- Node.js + npm

### 1. Start Casdoor and the databases

```bash
docker compose up -d
```

This brings up three containers: `casdoor` (auth server, port 8000), `casdoor-db` (MySQL, internal to Casdoor), and `giftcard-app-db` (Postgres, this app's own data — mapped to a non-default host port to avoid clashing with any locally installed Postgres, see Assumptions below).

### 2. Configure Casdoor

1. Open `http://localhost:8000`, log in as `admin` / `123` (Casdoor's own bootstrap admin — lives in its `built-in` organization, used only for configuring Casdoor itself, never as an end user of this app).
2. Create a new **Organization** (e.g. `giftcard-org`) for this platform's actual end users.
3. Create a new **Application** (e.g. `giftcard-app`), linked to that organization (not `built-in`), with:
   - Redirect URI: `http://localhost:5173/callback`
4. Copy the Application's **Client ID** and **Client Secret**.
5. Paste them into:
   - `frontend/src/pages/Login.jsx` (`CLIENT_ID`)
   - `backend/src/main/resources/application.yml` (`casdoor.client-id`, `casdoor.client-secret`)

### 3. Run the backend

```bash
cd backend
mvn spring-boot:run
```

On first run, Hibernate creates the schema and `data.sql` seeds two demo gift cards (Amazon, Flipkart) with vendor offers from both mock vendors. Runs on `http://localhost:8080`.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. Sign up as a new user through the app's login button (not directly on Casdoor's homepage, and not with the `admin`/`123` account — see Assumptions).

---

## Architecture Overview

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
└──────────────────┬───────────────────────┘
                    ▼
             PostgreSQL (gift_cards,
             gift_card_vendor_offers, orders)
```

**Why this split:**

- **Auth code exchange happens server-side** (`AuthController`), not in the browser. The alternative — the frontend calling Casdoor's token endpoint directly — would require shipping the Casdoor client secret in browser-loadable JS, which defeats the purpose of having a secret at all. The extra hop through our own backend keeps it server-side, at the cost of one more endpoint to maintain.
- **Vendor logic is isolated behind a single `VendorAggregatorService`**, with each vendor behind a common `VendorClient` interface. This means the merge/commission/failover logic doesn't know or care which vendor it's talking to — it just sorts offers by commission and tries them in order. Adding a third vendor later would mean writing one new `VendorClient` implementation and nothing else.
- **Vendor identity never leaves the backend.** `GiftCardVendorOffer` (which vendor, their SKU, their price) is a separate table from `GiftCard` and is never serialized into any DTO returned to the frontend — this was a hard requirement in the brief ("the user should only see the gift card and its price — not which vendor is fulfilling the order").

---

## Database Schema Explanation

Three core tables:

**`gift_cards`** — the catalog, exactly as the user sees it: title, category, image, description, terms, validity. No vendor information lives here.

**`gift_card_vendor_offers`** — one row per (gift card, vendor, denomination). This is the table that makes multi-vendor merging possible: it's what lets the same logical "Amazon Gift Card" have two different backing offers (QwikGift at ₹965, GiftBazaar at ₹970 for the ₹1000 denomination), each with its own stock status and vendor-specific product ID. Keeping this as a separate table (rather than, say, columns on `GiftCard` per vendor) means adding a third vendor is a data change, not a schema change.

**`orders`** — one row per placed order, storing the user (from the Casdoor JWT's `sub` claim), the gift card, denomination, price actually charged, status, and — internally only — which vendor fulfilled it (`fulfilledByVendor`). This column exists for auditing/debugging but is deliberately excluded from every API response DTO.

**Why PostgreSQL:** relational integrity matters here — an order always belongs to exactly one gift card, a vendor offer always belongs to exactly one gift card, and the commission calculation depends on consistent numeric types. Postgres also made the vendor-merge query (grouping offers by denomination, picking the best price) straightforward with standard SQL aggregation, which is a natural fit for JPA/Hibernate.

---

## Assumptions and Trade-offs

- **Price shown to the user** is currently the _lowest_ in-stock vendor's selling price for a given denomination (not the face value, and not a marked-up price). This also happens to always match whichever vendor the aggregator prefers, since the highest-commission vendor is mathematically the one with the lowest selling price. A real product would likely add a margin on top of this before showing it to the user; this was left as the simplest correct baseline.
- **Order cancellation** is implemented (`POST /api/orders/{id}/cancel`, gated to `PROCESSING` orders only) but rarely reachable in this demo: the mock vendor clients resolve synchronously and instantly, so an order transitions `PROCESSING → SUCCESS/FAILED` within the same request, before the frontend ever sees it as `PROCESSING`. This holds even when both vendors are set to fail — `VendorAggregatorService` tries every in-stock offer and returns a terminal `FAILED` result, it never leaves an order hanging. A production system with asynchronous vendor calls (queued jobs, webhook callbacks) would have a real window where `PROCESSING` persists long enough for a user to cancel; here, cancellation is correctly wired end-to-end but the synchronous mock flow doesn't exercise it.
- **Vendor mock images** use color-coded placeholder graphics (branded colors, no real logos) rather than pulling actual Amazon/Flipkart brand assets, to avoid trademark concerns in a submitted assignment.
- **Casdoor's `admin`/`123` account** is a `built-in`-organization administrator account, intentionally kept separate from this platform's own `giftcard-org` end users — Casdoor enforces this separation itself (an org-scoped Application will reject a `built-in` user, and vice versa), which surfaced during development and is treated here as correct, expected behavior rather than a bug.
- **The app's Postgres container runs on a non-default port** (mapped away from 5432/5433) specifically to avoid colliding with locally-installed PostgreSQL instances (e.g. from pgAdmin) on the development machine — a purely local-environment consideration, not something a deployed instance would need.
- **Vendor failure simulation** is a hardcoded boolean flag in each mock client (`QwikGiftClient`, `GiftBazaarClient`) rather than randomized, so that the failover path can be deterministically demonstrated on demand.
