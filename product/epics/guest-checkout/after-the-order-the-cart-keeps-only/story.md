---
id: after-the-order-the-cart-keeps-only
epic: guest-checkout
status: adopted
context: cart
feature: cartcheckout
title: After the order the cart keeps only what was not bought
depends_on: [place-the-order-from-the-review-and-see]
---

# After the order the cart keeps only what was not bought

## Story

As a guest who has placed an order I want what I bought to leave my cart, and what I added after starting
the checkout to stay there, so that the cart holds exactly what I still might buy.

## Acceptance criteria

<!-- happy-path: only-the-later-addition-remains -->

The cart changes shortly after the order is placed, not at the same moment: within 15 seconds.

### Rule: The order removes from the cart exactly the units it bought

#### only-the-later-addition-remains
- Given a guest who started the checkout over `"Bounded Context" Enamel Pin` twice and
  `"Domain over Framework" Hoodie` once
- And who then added `"Ports & Adapters" T-Shirt` once, so that the cart holds three lines
- When the guest places the order from the review
- Then the order is confirmed
- And shortly afterwards the cart page shows one line only: `"Ports & Adapters" T-Shirt` with quantity 1

#### a-bought-line-leaves-the-cart-completely
- Given a guest whose cart holds `"Bounded Context" Enamel Pin` three times and who started the checkout from it
- When the guest places the order from the review
- Then shortly afterwards the cart page reads "Your cart is empty"

#### units-added-after-the-start-stay
- Given a guest who started the checkout over `"Bounded Context" Enamel Pin` three times
- And who then added the pin twice more, so that the cart shows it five times
- When the guest places the order from the review
- Then shortly afterwards the cart page shows `"Bounded Context" Enamel Pin` with quantity 2

### Rule: The cart stays in use after the order

#### the-cart-accepts-new-items-again
- Given a guest whose order has left `"Ports & Adapters" T-Shirt` alone in the cart
- When the guest adds `"Domain over Framework" Hoodie` from its product page
- Then the cart page shows two lines, `"Ports & Adapters" T-Shirt` and `"Domain over Framework" Hoodie`
- And it offers "Proceed to Checkout"

## Out of scope

- The stock the order takes — `stock-goes-down-by-what-was-bought`.
- Lowering a line's quantity or removing a line between start and order: the pages offer neither, and the API
  cannot place an order.
- The same order confirmation arriving twice, and malformed confirmations: not observable from outside.
- A logged-in customer's cart after the order — `shop-with-an-account`.

## Notes

- Replay story: `CHK-10` (guest-checkout).
- Shared scenario: `scenario.checkout.completion-reconciles-snapshot`.
- Inventory: checkout 46; cart C-30, C-31, C-32 (later additions), C-34 (other lines), C-68.
