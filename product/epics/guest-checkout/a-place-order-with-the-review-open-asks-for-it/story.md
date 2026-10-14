---
id: a-place-order-with-the-review-open-asks-for-it
epic: guest-checkout
status: draft
context: checkout
feature: steps
title: A Place Order sent while the review is open asks for the review
depends_on: []
---

# A Place Order sent while the review is open asks for the review

## Story

As a guest whose "Place Order" reached the shop before I looked at the review I want the shop to lead me to the
review and say so, so that I place an order only after I have seen it.

## Acceptance criteria

### Rule: A "Place Order" that is refused while the review is open to the guest leads to the review

#### place-order-with-the-review-open-asks-for-the-review (happy path)
- Given a guest whose review step is open to them, but who has not placed the order from it
- When a "Place Order" is sent to the shop
- Then the guest ends on the review step at `/checkout/review`
- And the page says "Please review your order before placing it."
- And no confirmation is shown: the order is not placed

## Out of scope

- A "Place Order" sent while an earlier step is still missing: it keeps leading to the step still open with
  "Please complete the previous checkout steps first.", as the spec holds today.

## Assumptions

- open: Which state of the checkout is "the review is open to the guest, but the order is refused"? In both shops
  the review opens once the buyer, delivery and payment steps are submitted, and from then on a "Place Order" is
  accepted, whichever step page it is sent from; while one of the three is missing, it is refused with "Please
  complete the previous checkout steps first." Both shops carry "Please review your order before placing it." for a
  "Place Order" refused out of order, but no way through the pages reaches it. Is (a) a "Place Order" accepted only
  from the review page — then the shop has to learn that the guest is on it without a page that only opens changing
  state —, (b) the sentence dropped from both shops, as nothing reaches it, or (c) another state meant?

## Notes

- Decided by the user 2026-10-14 (WP-108 side effect T4): the Java shop's sentence in both shops, as a scenario in
  checkout/steps. Kept a draft until the assumption is answered: without a reachable state the scenario's `Given`
  cannot be set up, and its test cannot be red before a build.
