---
id: without-an-open-checkout-the-steps-lead
epic: guest-checkout
status: adopted
context: checkout
feature: steps
title: Without an open checkout the steps lead to the cart
depends_on: [place-the-order-from-the-review-and-see]
---

# Without an open checkout the steps lead to the cart

## Story

As a guest who has no open checkout — never started one, already placed the order, or the shop has been
restarted — I want the checkout pages to send me to my cart with a reason, and the confirmation of my order
to stay available, so that I never end up on an empty or broken step.

## Acceptance criteria

<!-- happy-path: buyer-step-without-a-checkout-leads-to-the-cart -->

### Rule: A step page without an open checkout leads to the cart with a message

#### buyer-step-without-a-checkout-leads-to-the-cart
- Given a visitor who has not started a checkout
- When the visitor opens `/checkout/buyer` directly
- Then the cart page opens at `/cart`
- And it shows the error "No active checkout session found"

#### later-step-without-a-checkout-leads-to-the-cart
- Given a visitor who has not started a checkout
- When the visitor opens `/checkout/review` directly
- Then the cart page opens with the error "No active checkout session found"

#### steps-after-the-order-lead-to-the-cart
- Given a guest who has placed an order and seen its confirmation
- When the guest opens `/checkout/buyer`
- Then the cart page opens with the error "No active checkout session found"

#### restart-of-the-shop-ends-an-open-checkout
- Given a guest with an open checkout on the delivery step
- And the shop has been restarted since
- When the guest opens `/checkout/delivery`
- Then the cart page opens with the error "No active checkout session found"

### Rule: The confirmation stays available after the order, and only after one

#### confirmation-can-be-reopened
- Given a guest who has placed an order for "Domain-Driven Design" once and has since browsed the catalogue
- When the guest opens `/checkout/confirmation`
- Then it reads "Your order has been placed successfully"
- And it shows "Domain-Driven Design x1 54.99 EUR" under "Items Ordered"

#### confirmation-without-an-order-leads-to-the-cart
- Given a visitor who has never placed an order
- When the visitor opens `/checkout/confirmation` directly
- Then the cart page opens with the error "No confirmed order found"

## Out of scope

- Opening a step ahead of the progress while a checkout is open — `the-steps-show-the-progress`.
- Another visitor's checkout — `a-visitor-never-reaches-another-visitors`.

## Notes

- Replay story: `CHK-08` (guest-checkout).
- Shared scenario: `scenario.checkout.no-session-redirects-to-cart`.
- Inventory: checkout 14, 43, 44, 49.
