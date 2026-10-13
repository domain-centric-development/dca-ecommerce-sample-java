---
id: created-product-answers-with-its-rounded-price
epic: shop-api
status: delivered
delivered: 2026-10-13T18:52:41Z
delivered_by: gate
context: product
feature: assortment
title: A created product answers with its price rounded to cents
depends_on: [staff-create-a-product-through-the-api]
---

# A created product answers with its price rounded to cents

## Story

As a member of staff who creates a product I want the answer to carry the price the shop holds — two decimals,
rounded half up — so that no fraction of a cent reaches anyone, the answer included.

## Acceptance criteria

### Rule: A price is rounded to two decimals, half up

#### the-answer-carries-the-rounded-price (happy path)
- Given a client holding a staff token
- When it creates "Rounding Mug" with SKU "ROUND-001", price 14.995, category "Desk & Office" and stock 5
- Then the shop answers 201 with `price` 15.00

## Changed expectations

- The 201 answer to `POST /api/products` carries the price as sent (14.995) now and the price the shop holds,
  rounded half up to cents (15.00), after.

## Notes

- The shop in the other language already behaves like this; this story closes the gap so that both hold the same spec
  (WP-107, decision `creating-a-product-holds-its-price-01`).
- A price is a decimal with two places and rounds by the commercial rule (half up), as `Money` already does.
