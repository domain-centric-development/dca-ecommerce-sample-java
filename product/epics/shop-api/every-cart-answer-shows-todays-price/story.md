---
id: every-cart-answer-shows-todays-price
epic: shop-api
status: delivered
delivered: 2026-10-21T20:59:46Z
delivered_by: gate
created: 2026-10-14T23:44:13Z
context: cart
feature: shopping
title: Every cart answer of the API shows today's prices
depends_on: [an-operator-changes-a-price, the-cart-holds-no-more-than-is-in-stock]
---

# Every cart answer of the API shows today's prices

## Story

As a partner application I want every answer about a cart to show the same prices, so that the total after adding a
product is the total I read back a moment later.

## Acceptance criteria

### Rule: Every answer about a cart prices its items at today's price, in the field `price`

#### adding-answers-at-todays-prices (happy path)
- Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API
- When her client adds "Clean Architecture" with quantity 1
- Then the shop answers 200 with the cart, whose "Domain-Driven Design" item has `price` 49.99 and whose `total` is
  89.98
- And no item of the answer carries a field `priceAtAddition`

#### removing-answers-at-todays-prices
- Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR, and "Clean Architecture" once
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API
- When her client removes "Clean Architecture"
- Then the shop answers 200 with the cart, whose "Domain-Driven Design" item has `price` 49.99 and whose `total` is
  49.99

## Out of scope

- The staff list of every cart: story `the-cart-oversight-totals-at-todays-prices` (feature `cartoversight`).
- The cart page: story `the-cart-shows-a-changed-price`.

## Changed expectations

- Adding to a cart and removing from it answer, where the shop answers so, each item at the price at which it was
  added, in a field `priceAtAddition`, while reading the cart answers today's price; after, every answer about a cart
  carries today's price, in the field `price`. The price at addition stays inside the shop.

## Assumptions

- answered: cart answers carry today's price, in the field `price` in both shops; the price at addition stays
  internal (planning/questions-2026-10-15.md Q4.6)
- answered: a — the two Test: lines are corrected on the main line (fa6873b8) to PaymentAtPlaceOrderIntegrationTest#choosingHowToPayRequestsNoPayment and #placeOrderRequestsThePaymentWithTheCheckoutReference; nothing in this story changes — merge the main line and run the build again. The cause, a Moved tests row naming the old test, is fixed in the pipeline (dca-factory 0.87.6). (every-cart-answer-shows-todays-price-01)

## Notes

- From TODO #95 (b), checked 2026-10-15: still true in the Java shop (adding and removing answer the price at
  addition, as `priceAtAddition`; reading answers today's); holds in the .NET shop (every single-cart answer uses
  today's price, as `price`). Hence `approved` in the Java shop and `adopted` in the .NET shop.
- The staff list's half went to its own story, `the-cart-oversight-totals-at-todays-prices`: the staff list is the
  feature `cartoversight` now (planning/questions-2026-10-15.md Q2), and one story changes one feature.
- Depends on `an-operator-changes-a-price` (the price change in the `Given`) and on
  `the-cart-holds-no-more-than-is-in-stock` (both change the cart API's answers, one after the other).
- Spec (`cart/shopping`): adds `adding-answers-at-todays-prices` and `removing-answers-at-todays-prices`; holds
  `adding-a-product-returns-the-cart`, `reading-the-own-cart` and `removing-a-product-removes-its-line`.
