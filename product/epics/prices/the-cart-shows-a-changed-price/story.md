---
id: the-cart-shows-a-changed-price
epic: prices
status: delivered
delivered: 2026-10-21T21:15:44Z
delivered_by: gate
created: 2026-10-15T07:50:52Z
context: cart
feature: shopping
title: A cart shows today's price after a price change
depends_on: [an-operator-changes-a-price, every-cart-answer-shows-todays-price]
---

# A cart shows today's price after a price change

## Story

As a customer whose cart holds a product whose price has changed I want the cart to show today's price and tell me
it changed, so that I am not surprised at the checkout.

## Acceptance criteria

### Rule: After a price change the cart prices the line at today's price and says that the price changed

#### the-cart-shows-todays-price-after-a-change (happy path)
- Given a guest whose cart holds "Domain-Driven Design" once, added at 54.99 EUR
- When a client holding a staff token sets the price of "Domain-Driven Design" to 49.99 EUR
- Then the guest's cart page shows the line "Domain-Driven Design" with "Unit Price" "49.99 EUR" and
  "Price decreased by 5.00 EUR"
- And the page shows "Some items have price changes since they were added to your cart."

## Out of scope

- The change itself: story `an-operator-changes-a-price`.
- The cart's answers through the API: story `every-cart-answer-shows-todays-price`.
- An open checkout: story `an-open-checkout-keeps-its-price`.

## Assumptions

- answered: after a price change the cart shows today's price (planning/questions-2026-10-15.md Q4.7)
- answered: the story is adopted — the cart page already shows today's price and the change notice once
  `an-operator-changes-a-price` is delivered; the factory maps it to a test with a break and the spec gains the key —
  Christoph Bloemer on 2026-10-21 in the .NET shop's decision the-cart-shows-a-changed-price-01 (option a), for both
  shops

## Notes

- From TODO #96 and Q4.7, 2026-10-15: the cart page already prices its lines at today's price and has a price-change
  notice that no scenario names; nothing outside the tests can change a price yet, so the scenario is red until
  `an-operator-changes-a-price` is delivered. Where the plan finds it green then, it recommends adopting.
- Spec (`cart/shopping`): adds `the-cart-shows-todays-price-after-a-change`; holds `cart-shows-lines-with-prices`.
