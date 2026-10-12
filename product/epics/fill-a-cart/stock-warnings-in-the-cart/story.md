---
id: stock-warnings-in-the-cart
epic: fill-a-cart
status: adopted
context: cart
feature: shopping
title: Stock warnings in the cart
depends_on: [the-cart-page, stock-goes-down-by-what-was-bought]
---

# Stock warnings in the cart

## Story

As a guest I want the cart to tell me on which line a product I collected has sold out or is short of stock
since I added it, so that I learn it before I try to check out.

## Acceptance criteria

<!-- happy-path: short-stock-line-warns -->

### Rule: A line whose quantity is more than the product's stock says so

#### short-stock-line-warns
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest whose cart holds "Wooden Hexagon Desk Model" with quantity 2
- And another guest has placed an order for 7 "Wooden Hexagon Desk Model", so 1 is left
- When the first guest opens the cart page
- Then the line "Wooden Hexagon Desk Model" shows "Insufficient stock available"

### Rule: A line whose product can no longer be bought says so, and only that

#### sold-out-line-says-unavailable
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest whose cart holds "Wooden Hexagon Desk Model" with quantity 1
- And another guest has placed an order for all 8 "Wooden Hexagon Desk Model"
- When the first guest opens the cart page
- Then the line "Wooden Hexagon Desk Model" shows "Product no longer available"
- And it does not show "Insufficient stock available"

### Rule: Only the line that is short of stock warns

#### only-the-short-line-warns
- Given a guest whose cart holds "Team Topologies" with quantity 1 and "Wooden Hexagon Desk Model" with quantity 2
- And another guest has placed an order for 7 "Wooden Hexagon Desk Model", so 1 is left
- When the first guest opens the cart page
- Then the page shows exactly one stock warning, on the line "Wooden Hexagon Desk Model"

## Out of scope

- The stock going down after an order is placed — `stock-goes-down-by-what-was-bought`.
- The disabled "Cannot Checkout" button with its note, and a checkout start refused for such a cart — `starting-and-placing-check-the-cart`.
- Adding a product that is out of stock: the product page offers no "Add to Cart" for it — `availability-shown`.

## Notes

- Replay story: `CRT-05` (fill-a-cart).
- A product can be bought while it has stock left; a line has enough stock while its quantity is at most the
  stock. The only way stock goes down from outside is an order another visitor places.
- The warnings are the cart page's; what they block at the checkout belongs to the checkout.
- Inventory: cart C-22.
