---
id: start-the-checkout-from-the-cart
epic: guest-checkout
status: adopted
context: checkout
feature: session
title: Start the checkout from the cart
depends_on: [the-cart-page]
---

# Start the checkout from the cart

## Story

As a guest who has decided what to buy I want to start the checkout straight from my cart, without an
account, so that I can turn the cart into an order.

## Acceptance criteria

<!-- happy-path: proceed-opens-the-buyer-step -->

### Rule: A cart whose every line can be bought offers the checkout

#### cart-offers-proceed-to-checkout
- Given a guest whose cart holds "Domain-Driven Design" once
- When the guest opens the cart page
- Then it shows a "Proceed to Checkout" button
- And a "Continue Shopping" link to the catalogue page `/products`

### Rule: Starting the checkout opens the buyer step

#### proceed-opens-the-buyer-step
- Given a guest whose cart holds "Domain-Driven Design" once
- When the guest presses "Proceed to Checkout" on the cart page
- Then the buyer step opens at `/checkout/buyer`
- And no login or registration was asked for

### Rule: The checkout copies the cart's lines at today's catalogue price

#### buyer-step-summarises-the-cart
- Given a guest whose cart holds "Domain-Driven Design" twice and "Clean Architecture" once
- When the guest presses "Proceed to Checkout" on the cart page
- Then the buyer step shows an "Order Summary" with one entry per line, each with the product's image, its
  name, the quantity as "x<quantity>" and the line total
- And the entries read "Domain-Driven Design x2 109.98 EUR" and "Clean Architecture x1 39.99 EUR"
- And the summary's Subtotal reads "149.97 EUR"

## Out of scope

- The buyer form on the buyer step — `enter-the-buyer-details`.
- The progress bar on the step pages — `the-steps-show-the-progress`.
- The disabled "Cannot Checkout" button, and a start refused because the cart changed underneath the page —
  `starting-and-placing-check-the-cart`.
- Starting the checkout with someone else's cart — `a-visitor-never-reaches-another-visitors`.
- Starting the checkout again while one is open — `the-open-checkout-keeps-its-order-while`.
- The login and registration offer on the buyer step, and a logged-in customer's checkout —
  `shop-with-an-account`.

## Notes

- Replay story: `CHK-01` (guest-checkout).
- Inventory: checkout 1 (enabled button), 2, 3, 16 (buyer step); cart C-23.
