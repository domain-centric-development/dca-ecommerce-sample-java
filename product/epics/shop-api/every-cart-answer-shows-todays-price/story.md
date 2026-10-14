---
id: every-cart-answer-shows-todays-price
epic: shop-api
status: draft
context: cart
feature: shopping
title: Every cart answer of the API shows today's prices
depends_on: []
---

# Every cart answer of the API shows today's prices

## Story

As a partner application I want every answer about a cart to show the same prices, so that the total after adding a
product is the total I read back a moment later.

## Acceptance criteria

### Rule: Every answer about a cart prices its lines at today's price

#### adding-answers-at-todays-prices (happy path)
- Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR
- When her client adds "Clean Architecture" with quantity 1
- Then the shop answers 200 with the cart, whose "Domain-Driven Design" item is priced 49.99 and whose `total` is
  89.98

#### the-staff-list-totals-at-todays-prices
- Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR
- When a client holding a staff token sends `GET /api/carts`
- Then Ada's cart in the answer has `total` 49.99

## Changed expectations

- Reading a cart answers today's prices now, while adding to it, removing from it and the staff list answer the
  prices at addition, so the same cart has two totals; after, every answer uses today's price.

## Assumptions

- open: which price is wanted in the API — today's price everywhere (as written, as the cart page shows), or the
  price at addition everywhere?
- open: the item's price field — the two shops name it differently (`priceAtAddition` / `price`); which name do both
  answer with, and does a name that says "at addition" still fit a price of today?

## Notes

- From TODO #95 (b), checked 2026-10-15: still true in the Java shop (adding and removing answer the price at
  addition, reading answers today's); partly in the .NET shop (every single-cart answer uses today's price, the staff
  list's total the price at addition).
- The price change in the `Given` has no surface yet (TODO #96, story `an-operator-changes-a-price`); the test sets it
  through the price owner.
- Spec (`cart/shopping`): adds `adding-answers-at-todays-prices` and `the-staff-list-totals-at-todays-prices`; holds
  `adding-a-product-returns-the-cart`, `reading-the-own-cart` and `staff-list-every-cart`.
