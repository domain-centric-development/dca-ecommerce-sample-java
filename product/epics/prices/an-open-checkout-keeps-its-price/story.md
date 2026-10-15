---
id: an-open-checkout-keeps-its-price
epic: prices
status: approved
context: checkout
feature: ordering
title: An open checkout keeps the price it started with
depends_on: [an-operator-changes-a-price, two-orders-never-sell-more-than-the-stock]
---

# An open checkout keeps the price it started with

## Story

As a customer in the middle of a checkout I want the price I am reviewing to be the price I order at, so that a price
change while I check out does not change my order behind my back.

## Acceptance criteria

### Rule: A checkout keeps the prices it started with, whatever price changes come after

#### an-open-checkout-keeps-the-price-it-started-with (happy path)
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", started at 54.99 EUR
- When a client holding a staff token sets the price of "Domain-Driven Design" to 49.99 EUR
- Then the guest's review still shows "Domain-Driven Design x1 54.99 EUR" and Total "59.98 EUR"

#### the-order-is-placed-at-the-checkouts-price
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", started at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API
- When the guest presses "Place Order"
- Then the confirmation page shows under "Items Ordered" "Domain-Driven Design x1 54.99 EUR"

## Out of scope

- The change itself: story `an-operator-changes-a-price`.
- A cart: story `the-cart-shows-a-changed-price`.
- A checkout started after the change: it starts at today's price, as every checkout does.

## Assumptions

- answered: an open checkout keeps its snapshot of the prices (planning/questions-2026-10-15.md Q4.7)

## Notes

- From TODO #96 and Q4.7, 2026-10-15: nothing outside the tests can change a price yet, so both scenarios are red until
  `an-operator-changes-a-price` is delivered. Where the plan finds them green then, it recommends adopting.
- Depends on `two-orders-never-sell-more-than-the-stock`: both change `checkout/ordering`, one after the other.
- Spec (`checkout/ordering`): adds `an-open-checkout-keeps-the-price-it-started-with` and
  `the-order-is-placed-at-the-checkouts-price`; holds `confirmation-shows-what-was-ordered`.
