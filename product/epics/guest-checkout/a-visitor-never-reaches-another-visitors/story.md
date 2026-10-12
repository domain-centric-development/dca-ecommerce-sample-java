---
id: a-visitor-never-reaches-another-visitors
epic: guest-checkout
status: adopted
context: checkout
feature: session
title: A visitor never reaches another visitor's checkout
depends_on: [without-an-open-checkout-the-steps-lead]
---

# A visitor never reaches another visitor's checkout

## Story

As a guest in the checkout I want nobody else to see, fill in or start my checkout, so that my details and
my order stay mine.

## Acceptance criteria

<!-- happy-path: another-visitor-finds-no-checkout -->

In every scenario "the owner" is a guest who started the checkout from a cart holding "Domain-Driven
Design" once and submitted the buyer step with "jane@example.com"; "the stranger" uses a separate browser.

### Rule: Checkout pages only ever show the visitor's own checkout

#### another-visitor-finds-no-checkout
- Given the owner's open checkout and a stranger who has not started one
- When the stranger opens `/checkout/buyer`
- Then the stranger's cart page opens with the error "No active checkout session found"
- And nothing of the owner's checkout is shown to the stranger

### Rule: A checkout starts only from the visitor's own cart

#### starting-from-a-strangers-cart-is-refused
- Given the owner's cart and a stranger who knows its cart id
- When the stranger sends the start-checkout form with the owner's cart id
- Then the stranger's cart page opens with an error beginning "Cart not found:"
- And the owner's buyer step still shows "Domain-Driven Design x1" and "jane@example.com"

## Out of scope

- A logged-in customer's checkout and cart — `shop-with-an-account`.
- The cart itself being reached by someone else — `fill-a-cart`.

## Notes

- Replay story: `CHK-13` (guest-checkout).
- Inventory: checkout 6 (someone else's cart), 41; cart C-56.
