---
id: open-and-fill-a-cart-through-the-api
epic: shop-api
status: adopted
context: cart
feature: shopping
title: Open and fill a cart through the API
depends_on: [register-through-the-api, read-the-catalogue-through-the-api, add-a-product-to-the-cart]
---

# Open and fill a cart through the API

## Story

As a partner application I want to open a cart for my signed-in customer and put products into it through the
API, so that my customer can shop in my application without the shop's pages.

## Acceptance criteria

<!-- happy-path: adding-a-product-returns-the-cart -->

### Rule: A cart is opened for the caller, who is always its customer

#### opening-a-cart-for-the-caller
- Given a client holding the token of the customer "ada@example.com", who has no cart yet
- When it sends `POST /api/carts`
- Then the shop answers 201
- And the body has a `cartId`, a `customerId`, `items` [], `status` "ACTIVE" and `itemCount` 0

#### a-second-open-cart-is-a-conflict
- Given the customer "ada@example.com" already opened a cart through the API
- When the client sends `POST /api/carts` with her token again
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Active cart already exists"

#### the-callers-active-cart-by-customer-id
- Given the customer "ada@example.com", whose `customerId` the shop returned when she opened her cart
- When the client sends `GET /api/carts/customer/<her customerId>/active` with her token
- Then the shop answers 200 with that cart, same `cartId`, `status` "ACTIVE"

### Rule: Adding a product puts it in the cart; the same product stays on one line

#### adding-a-product-returns-the-cart
- Given Ada's open cart is empty
- When the client sends `POST /api/carts/<cartId>/items` with `{"productId":"<id of Domain-Driven Design>","quantity":2}`
- Then the shop answers 200 with the cart
- And it has one item with that `productId` and `quantity` 2, `total` 109.98, `currency` "EUR" and `itemCount` 1

#### adding-the-same-product-again-adds-up
- Given Ada's cart holds "Domain-Driven Design" × 2
- When the client adds "Domain-Driven Design" with quantity 1
- Then the cart still has one item, now with `quantity` 3, and `total` 164.97

#### reading-the-own-cart
- Given Ada's cart holds "Domain-Driven Design" × 2 and "Team Topologies" × 1
- When the client sends `GET /api/carts/<cartId>` with her token
- Then the shop answers 200 with two items, `total` 139.97, `currency` "EUR" and `itemCount` 2

### Rule: Removing a product takes its whole line out

#### removing-a-product-removes-its-line
- Given Ada's cart holds "Domain-Driven Design" × 3 and "Team Topologies" × 1
- When the client sends `DELETE /api/carts/<cartId>/items/<id of Domain-Driven Design>`
- Then the shop answers 200 with the cart
- And it has one item, "Team Topologies" with `quantity` 1, and `itemCount` 1

## Out of scope

- A cart item's unit price field: not replayed.
- Refusals when adding or removing — `the-cart-api-refuses-what-the-cart`; other people's carts and the staff list — `only-your-own-cart-through-the-api`.
- Starting a checkout through the API: no such route exists.
- Adding to the cart on the pages — `add-a-product-to-the-cart`.

## Notes

- Replay story: `API-06` (shop-api).
- Inventory: cart C-10 (through the API), C-57 (own cart), C-58, C-59, C-62 (own id), C-63, C-64.
