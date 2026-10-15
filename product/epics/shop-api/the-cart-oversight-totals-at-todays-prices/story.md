---
id: the-cart-oversight-totals-at-todays-prices
epic: shop-api
status: approved
context: cart
feature: cartoversight
title: The cart oversight totals every cart at today's prices
depends_on: [the-cart-oversight-is-a-feature-of-its-own, an-operator-changes-a-price]
---

# The cart oversight totals every cart at today's prices

## Story

As staff who look after the shop I want the list of every cart to total each cart at today's prices, so that a cart
in the list has the total its customer reads.

## Acceptance criteria

### Rule: The cart oversight totals each cart at today's prices

#### the-staff-list-totals-at-todays-prices (happy path)
- Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API
- When a client holding a staff token sends `GET /api/carts`
- Then Ada's cart in the answer has `total` 49.99

## Out of scope

- The answers about one cart: story `every-cart-answer-shows-todays-price`.

## Changed expectations

- The staff list totals each cart at the prices at which its products were added now; after, at today's prices, as
  every answer about one cart does.

## Assumptions

- answered: cart answers carry today's price; the price at addition stays internal (planning/questions-2026-10-15.md
  Q4.6)

## Notes

- From TODO #95 (b), checked 2026-10-15: still true in both shops — the staff list's total uses the price at addition.
- Split from `every-cart-answer-shows-todays-price`: the staff list is the feature `cartoversight` now
  (planning/questions-2026-10-15.md Q2), and one story changes one feature.
- Depends on `the-cart-oversight-is-a-feature-of-its-own` (the feature this changes) and on
  `an-operator-changes-a-price` (the price change in the `Given`).
- Spec (`cart/cartoversight`): adds `the-staff-list-totals-at-todays-prices`; holds `staff-list-every-cart`.
