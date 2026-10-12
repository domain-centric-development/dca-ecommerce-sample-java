---
id: the-cart-api-refuses-what-the-cart
epic: shop-api
status: adopted
context: cart
feature: shopping
title: The cart API refuses what the cart cannot take
depends_on: [open-and-fill-a-cart-through-the-api]
---

# The cart API refuses what the cart cannot take

## Story

As a partner application I want the cart API to tell me why it refuses a change, so that I can show my customer
what went wrong instead of a broken cart.

## Acceptance criteria

<!-- happy-path: more-than-in-stock-is-refused -->

### Rule: A product the catalogue does not know cannot be added

#### an-unknown-product-is-not-available
- Given Ada's open, empty cart
- When her client sends `POST /api/carts/<cartId>/items` with `{"productId":"no-such-product","quantity":1}`
- Then the shop answers 404 with an `application/problem+json` body
- And its title is "Article not available" and its detail "Product not found: no-such-product"

### Rule: No more can be added than is in stock

#### more-than-in-stock-is-refused
- Given Ada's open, empty cart and "Wooden Hexagon Desk Model" with 8 in stock
- When her client adds "Wooden Hexagon Desk Model" with quantity 9
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Not enough stock" and its detail "Insufficient stock for product: <id of Wooden Hexagon Desk Model>"
- And her cart is still empty

#### exactly-the-stock-is-accepted
- Given Ada's open, empty cart and "Wooden Hexagon Desk Model" with 8 in stock
- When her client adds "Wooden Hexagon Desk Model" with quantity 8
- Then the shop answers 200 and the cart holds it with `quantity` 8

### Rule: A quantity is at least 1

#### a-quantity-of-zero-is-refused
- Given Ada's open, empty cart
- When her client adds "Domain-Driven Design" with quantity 0
- Then the shop answers 400
- And her cart is still empty

### Rule: Removing a product the cart does not hold is refused

#### removing-a-product-not-in-the-cart
- Given Ada's cart holds only "Domain-Driven Design" × 1
- When her client sends `DELETE /api/carts/<cartId>/items/<id of Team Topologies>`
- Then the shop answers 404 with an `application/problem+json` body
- And its title is "Position not in cart" and its detail "Product not found in cart: <id of Team Topologies>"

## Out of scope

- The body of the 400 for a quantity of zero: its shape is not replayed.
- Changing a cart that is no longer active: nothing in the shop completes or abandons a cart today, so it is not
  reachable from outside.
- The same refusals on the pages: the page form always sends quantity 1 and only for available products.

## Notes

- Replay story: `API-08` (shop-api).
- Inventory: cart C-12, C-13, C-15 (through the API), C-63 (refusals), C-64 (position not in cart), C-65
  (reachable titles); cross-cutting X14 (cart titles).
