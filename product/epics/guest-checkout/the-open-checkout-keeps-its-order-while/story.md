---
id: the-open-checkout-keeps-its-order-while
epic: guest-checkout
status: adopted
context: checkout
feature: cartsync
title: The open checkout keeps its order while the cart changes
depends_on: [enter-the-buyer-details]
---

# The open checkout keeps its order while the cart changes

## Story

As a guest with an open checkout I want to keep changing my cart without the checkout changing under me,
and to take the changed cart into a new checkout by starting it again, so that the order I review is the
one I started and I decide when the cart's changes count.

## Acceptance criteria

<!-- happy-path: open-checkout-keeps-its-summary -->

### Rule: Cart changes leave the open checkout as it was started

#### open-checkout-keeps-its-summary
- Given a guest whose cart holds `"Bounded Context" Enamel Pin` twice and who started the checkout from it
- And who then added `"Bounded Context" Enamel Pin` three more times and `"Domain over Framework" Hoodie` once,
  so that the cart shows two lines with the pin five times
- When the guest opens `/checkout/buyer`
- Then the order summary still shows only `"Bounded Context" Enamel Pin x2`

### Rule: Starting the checkout again replaces the open one with the current cart

#### restart-takes-the-current-cart
- Given a guest with an open checkout over `"Bounded Context" Enamel Pin` twice whose cart now holds the pin
  five times and `"Domain over Framework" Hoodie` once
- When the guest presses "Proceed to Checkout" on the cart page again
- Then the buyer step's order summary shows `"Bounded Context" Enamel Pin x5` and
  `"Domain over Framework" Hoodie x1`
- And the Subtotal reads "124.94 EUR"

#### restart-begins-with-empty-forms
- Given a guest who submitted the buyer step with "jane@example.com", "Jane", "Doe", "+49 30 1234567" and then
  returned to the cart
- When the guest presses "Proceed to Checkout" on the cart page again
- Then the buyer step opens with all fields empty

## Out of scope

- What the cart keeps after the order — `after-the-order-the-cart-keeps-only`.
- A checkout whose products ran short of stock in the meantime — `starting-and-placing-check-the-cart`.

## Notes

- Replay story: `CHK-09` (guest-checkout).
- Shared scenario: `scenario.checkout.snapshot-survives-cart-edit`.
- Inventory: checkout 7, 8, 9, 13 (a cart change does not reach the open checkout); cart C-29.
