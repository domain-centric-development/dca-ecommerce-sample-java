---
id: the-cart-oversight-totals-at-todays-prices-accept-1
story: the-cart-oversight-totals-at-todays-prices
stage: document
kind: acceptance
asked: 2026-10-20T17:13:40Z
digest: 43378e852e8e773fe8263bac7749ef878bfa97a7d87957fcace68f9fd4e04b37
---

# Accept the-cart-oversight-totals-at-todays-prices?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-staff-list-totals-at-todays-prices: Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR; And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API; When a client holding a staff token sends `GET /api/carts`; Then Ada's cart in the answer has `total` 49.99 — `dev.domaincentric.sample.ecommerce.e2e.CartOversightPriceE2ETest#theStaffListTotalsEachCartAtTheProductsCurrentPrice`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-20T18:32:15Z
