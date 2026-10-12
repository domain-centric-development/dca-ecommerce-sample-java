---
id: the-steps-show-the-progress
epic: guest-checkout
status: adopted
context: checkout
feature: steps
title: The steps show the progress and are taken in order
depends_on: [place-the-order-from-the-review-and-see]
---

# The steps show the progress and are taken in order

## Story

As a guest in the checkout I want to see which steps I have done and which one I am on, and I cannot skip a
step by accident, so that I always know how far the order is and nothing is missing from it.

## Acceptance criteria

<!-- happy-path: progress-bar-on-the-review -->

### Rule: Every step page shows a four-step progress bar

#### progress-bar-on-the-buyer-step
- Given a guest who has just started the checkout
- When the guest opens the buyer step
- Then a progress bar shows the steps "Buyer Info", "Delivery", "Payment" and "Review" in this order, numbered
  1 to 4
- And "Buyer Info" is marked as the current step and none as done

#### progress-bar-on-the-review
- Given a guest who has submitted the buyer, delivery and payment steps
- When the guest opens the review step
- Then the progress bar marks "Buyer Info", "Delivery" and "Payment" as done and "Review" as the current step

#### progress-bar-on-the-confirmation
- Given a guest on the review step
- When the guest presses "Place Order"
- Then the confirmation page's progress bar shows "Buyer Info", "Delivery", "Payment" and "Confirmed", all
  four marked as done

### Rule: A step ahead of the progress opens the current step instead

#### skipping-ahead-opens-the-current-step
- Given a guest who has submitted the buyer step and is on the delivery step
- When the guest opens `/checkout/review` directly
- Then the delivery step opens at `/checkout/delivery`

#### payment-from-the-buyer-step-opens-the-buyer-step
- Given a guest who has just started the checkout
- When the guest opens `/checkout/payment` directly
- Then the buyer step opens at `/checkout/buyer`

### Rule: A step's form sent before the earlier steps are done changes nothing

#### delivery-sent-before-the-buyer-step
- Given a guest who has just started the checkout and has not submitted the buyer step
- When a delivery form with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany" and "Standard Shipping" is sent to the shop
- Then the guest ends on the buyer step at `/checkout/buyer`
- And the review cannot be opened afterwards: `/checkout/review` opens the buyer step

#### place-order-before-the-review
- Given a guest who has submitted the buyer and delivery steps and is on the payment step
- When a "Place Order" is sent to the shop
- Then the guest ends on the payment step at `/checkout/payment`
- And no confirmation is shown: the order is not placed

## Out of scope

- The "Back" and "Edit" links to earlier steps — `go-back-to-an-earlier-step-and-change-it`.
- Step pages without any checkout, and after the order — `without-an-open-checkout-the-steps-lead`.

## Notes

- Replay story: `CHK-06` (guest-checkout).
- Inventory: checkout 15, 17, 30, 34 (step order), 40.
