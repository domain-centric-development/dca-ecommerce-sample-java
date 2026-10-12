---
id: go-back-to-an-earlier-step-and-change-it
epic: guest-checkout
status: adopted
context: checkout
feature: steps
title: Go back to an earlier step and change it
depends_on: [place-the-order-from-the-review-and-see]
---

# Go back to an earlier step and change it

## Story

As a guest in the checkout I want to go back to a step I have done and change what I entered, without typing
everything again and without losing the steps after it, so that a mistake does not cost me the checkout.

## Acceptance criteria

<!-- happy-path: back-from-delivery-opens-the-buyer-step -->

In every scenario the guest started the checkout with "Domain-Driven Design" once and submitted the buyer
step with "jane@example.com", "Jane", "Doe", "+49 30 1234567".

### Rule: Back links lead one step back, with the form filled in

#### back-from-delivery-opens-the-buyer-step
- Given the guest is on the delivery step
- When the guest follows the delivery step's "Back" link
- Then the buyer step opens at `/checkout/buyer`
- And its fields hold "jane@example.com", "Jane", "Doe" and "+49 30 1234567"

#### back-from-payment-opens-the-delivery-step
- Given the guest submitted the delivery step with "Hauptstrasse 1", "Berlin", "10115", "Germany" and
  "Standard Shipping" and is on the payment step
- When the guest follows the payment step's "Back" link
- Then the delivery step opens with these address fields filled in and "Standard Shipping" chosen

#### back-from-review-opens-the-payment-step
- Given the guest submitted the delivery step, chose "Mock Payment (Test)" and is on the review step
- When the guest follows the review's "Back" link
- Then the payment step opens with "Mock Payment (Test)" chosen

### Rule: Each section of the review has an Edit link to its step

#### edit-links-on-the-review
- Given the guest submitted the delivery step, chose "Mock Payment (Test)" and is on the review step
- When the guest follows the "Edit" link of the "Delivery" section
- Then the delivery step opens with the address filled in and "Standard Shipping" chosen
- And the review's "Buyer Information" and "Payment Method" sections carry "Edit" links to the buyer step and
  the payment step

### Rule: Changing an earlier step replaces its data and keeps the later progress

#### changed-shipping-recalculates-the-totals
- Given the guest submitted the delivery step with "Standard Shipping", chose "Mock Payment (Test)" and has
  reopened the delivery step from the review
- When the guest submits the delivery step again with "Express Shipping"
- Then the payment step opens showing Shipping "9.99 EUR" and Total "64.98 EUR"

#### changed-step-keeps-the-later-progress
- Given the guest reached the review with "Standard Shipping" and "Mock Payment (Test)", then submitted the
  delivery step again with "Express Shipping"
- When the guest opens `/checkout/review` directly
- Then the review opens, showing "Express Shipping", "Estimated Delivery: 2-3 business days" and
  "Cost: 9.99 EUR"
- And Subtotal "54.99 EUR", Shipping "9.99 EUR", Tax "10.37 EUR" and Total "64.98 EUR"
- And the "Payment Method" section still names the chosen payment provider

#### changed-buyer-details-reach-the-review
- Given the guest reached the review with "Standard Shipping" and "Mock Payment (Test)" and has reopened the
  buyer step from the review
- When the guest submits the buyer step again with Email Address "jane.doe@example.com"
- Then the delivery step opens with the address still filled in
- And the review, opened directly, shows "jane.doe@example.com"

## Out of scope

- The progress bar and the order in which steps may be opened — `the-steps-show-the-progress`.

## Notes

- Replay story: `CHK-07` (guest-checkout).
- Shared scenario: `scenario.checkout.back-navigation`.
- Inventory: checkout 18, 19, 31 (recalculated on change).
