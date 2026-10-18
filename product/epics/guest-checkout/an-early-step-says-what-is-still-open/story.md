---
id: an-early-step-says-what-is-still-open
epic: guest-checkout
status: delivered
delivered: 2026-10-14T05:06:13Z
delivered_by: gate
created: 2026-10-13T20:32:57Z
context: checkout
feature: steps
title: A step sent too early says that the earlier steps come first
depends_on: [the-steps-show-the-progress]
---

# A step sent too early says that the earlier steps come first

## Story

As a guest whose step form reached the shop before the earlier steps were done I want the step I am sent to to say
why, so that I know what to do instead of wondering why the page changed.

## Acceptance criteria

### Rule: A step's form sent before the earlier steps are done changes nothing

#### delivery-sent-before-the-buyer-step (happy path)
- Given a guest who has just started the checkout and has not submitted the buyer step
- When a delivery form with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany" and "Standard Shipping" is sent to the shop
- Then the guest ends on the buyer step at `/checkout/buyer`
- And the page says "Please complete the previous checkout steps first."
- And the review cannot be opened afterwards: `/checkout/review` opens the buyer step

#### place-order-before-the-review
- Given a guest who has submitted the buyer and delivery steps and is on the payment step
- When a "Place Order" is sent to the shop
- Then the guest ends on the payment step at `/checkout/payment`
- And the page says "Please complete the previous checkout steps first."
- And no confirmation is shown: the order is not placed

## Changed expectations

- A step form sent before the earlier steps are done lands on the step still open without a word now, and with
  "Please complete the previous checkout steps first." after.

## Notes

- Decided by the user 2026-10-13 (WP-108 K3): the customer is told why the shop sent them elsewhere. Both shops
  already hold the sentence for the refusal; neither shows it after the redirect today.
- The shop runs as several instances without sticky sessions (`product/tech.md`, Runtime): the sentence travels with
  the redirect to the step still open — not in a server session, a flash attribute or TempData — so any instance
  that answers the next request shows it.
