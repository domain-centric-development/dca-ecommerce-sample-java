---
id: checkout-refusals-say-what-happened
epic: provider-payment
status: draft
created: 2026-10-23T21:25:20Z
context: checkout
feature: ordering
title: Checkout refusals say what happened
closes: [the-shop-sets-the-shipping-price#1]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# Checkout refusals say what happened

## Story

As a buyer whose step is refused I want the refusal to name what happened, so that I know what to do next.

## Acceptance criteria

### Rule: A refused step names what changed

#### a-changed-price-is-named-as-a-price-change (happy path)
- Given a buyer on the review of "Clean Architecture" at 39.99
- And the price of "Clean Architecture" changed to 44.99
- When the buyer presses "Place Order"
- Then the review shows that a price changed, not that an item is unavailable

#### a-changed-quantity-is-not-named-a-price-change
- Given a buyer on the review of "Clean Architecture" once
- And the cart's quantity of "Clean Architecture" changed to 2 in another tab
- When the buyer presses "Place Order"
- Then the review shows that the cart changed, not that a price changed

## Assumptions

- open: the words of each refusal: a way to pay withdrawn before Place Order, prices that changed, a changed quantity or product, a shipping method the shop does not offer

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- Each refusal is a row of the findings; the shop that already says it right adopts the criterion.
