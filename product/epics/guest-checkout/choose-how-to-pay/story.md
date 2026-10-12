---
id: choose-how-to-pay
epic: guest-checkout
status: adopted
context: checkout
feature: payment
title: Choose how to pay
depends_on: [choose-the-delivery-address]
---

# Choose how to pay

## Story

As a guest in the checkout I want to see what I will pay including shipping and choose how to pay, so
that I can go on to review the order.

## Acceptance criteria

<!-- happy-path: choosing-the-provider-opens-the-review -->

### Rule: The payment step offers the payment providers that are available

#### payment-step-offers-the-provider
- Given a guest who submitted the delivery step with "Standard Shipping"
- When the guest opens the payment step
- Then it shows under "Select Payment Method" exactly one choice, "Mock Payment (Test)"
- And a "Back" link and a "Continue to Review" button

### Rule: The chosen shipping price is added to the order total

#### payment-step-totals-with-standard-shipping
- Given a guest who started the checkout with "Domain-Driven Design" once and chose "Standard Shipping"
- When the guest opens the payment step
- Then its "Order Summary" shows "Domain-Driven Design x1 54.99 EUR"
- And Subtotal "54.99 EUR", Shipping "4.99 EUR" and Total "59.98 EUR"

#### payment-step-totals-with-express-shipping
- Given a guest who started the checkout with "Domain-Driven Design" once and chose "Express Shipping"
- When the guest opens the payment step
- Then it shows Subtotal "54.99 EUR", Shipping "9.99 EUR" and Total "64.98 EUR"

### Rule: Choosing a provider starts the payment and moves on to the review

#### choosing-the-provider-opens-the-review
- Given a guest on the payment step
- When the guest chooses "Mock Payment (Test)" and presses "Continue to Review"
- Then the review step opens at `/checkout/review`

#### unknown-provider-is-refused
- Given a guest on the payment step
- When the payment form is sent with the provider "paypal"
- Then the payment step opens again with the error "This way to pay is not offered. Please choose another one."

## Out of scope

- What the review shows, including the payment reference — `place-the-order-from-the-review-and-see`.
- Choosing the payment again from the review — `go-back-to-an-earlier-step-and-change-it`.
- Sending the payment form before the delivery step is done — `the-steps-show-the-progress`.
- A provider that is unavailable or fails, and a total of nothing to pay: the one provider the shop has is
  always available and always succeeds, and every product has a positive price.

## Notes

- Replay story: `CHK-04` (guest-checkout).
- Inventory: checkout 16 (payment step), 31 (shipping in the total), 32, 33, 35 (unknown provider).
