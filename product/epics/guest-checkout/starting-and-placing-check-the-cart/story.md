---
id: starting-and-placing-check-the-cart
epic: guest-checkout
status: adopted
context: checkout
feature: ordering
title: Starting and placing check the cart against what can still be sold
depends_on: [after-the-order-the-cart-keeps-only, stock-goes-down-by-what-was-bought]
---

# Starting and placing check the cart against what can still be sold

## Story

As a shop owner I want the checkout to start and the order to be placed only while every product in it can
still be sold in the quantity asked for, so that the shop does not take orders it cannot deliver.

## Acceptance criteria

<!-- happy-path: place-order-is-refused-when-stock-ran-short -->

In every scenario "Wooden Hexagon Desk Model" starts with its seeded stock of 8, and "another visitor"
places an order in a separate browser; the stock has gone down by that order before the guest acts.

### Rule: A cart with a line short of stock cannot start the checkout

#### cart-shows-cannot-checkout-when-stock-ran-short
- Given a guest whose cart holds "Wooden Hexagon Desk Model" five times
- And another visitor has since bought it five times, leaving 3
- When the guest opens the cart page
- Then instead of "Proceed to Checkout" it shows a disabled "Cannot Checkout" button
- And the note "Some items are unavailable or have insufficient stock"

#### start-from-an-outdated-cart-page-is-refused
- Given a guest whose cart page, opened while it held "Wooden Hexagon Desk Model" five times with 8 in stock,
  still shows "Proceed to Checkout"
- And another visitor has since bought it five times, leaving 3
- When the guest presses "Proceed to Checkout" on that page
- Then the cart page opens with the error "Cannot start checkout, 1 item(s) unavailable or out of stock"

#### start-from-an-emptied-cart-is-refused
- Given a guest whose cart page, opened while it held "Domain-Driven Design" once, still shows
  "Proceed to Checkout"
- And the guest has since placed an order for exactly that cart in another tab, and the cart is now empty
- When the guest presses "Proceed to Checkout" on the old page
- Then the cart page opens with an error beginning "Cannot checkout empty cart"

### Rule: Place Order checks every line again and changes nothing when one fails

#### place-order-is-refused-when-stock-ran-short
- Given a guest at the review with "Wooden Hexagon Desk Model" five times
- And another visitor has since bought it five times, leaving 3
- When the guest presses "Place Order"
- Then the review step stays open at `/checkout/review` and shows an error
- And no confirmation is shown
- And the product page still reads "3 units available" and the guest's cart still holds the product five
  times

#### place-order-is-refused-when-sold-out
- Given a guest at the review with "Wooden Hexagon Desk Model" twice
- And another visitor has since bought all 8
- When the guest presses "Place Order"
- Then the review step stays open at `/checkout/review` and shows an error
- And no confirmation is shown

## Out of scope

- A price that changed after the checkout started: the shop offers no way to change a price.
- Stock set aside while a checkout is open: the shop reserves nothing today.
- The wording of the error a refused "Place Order" shows.

## Notes

- Replay story: `CHK-12` (guest-checkout).
- Inventory: checkout 1 (disabled button), 4, 5, 38 (availability and stock); cart C-24, C-25.
