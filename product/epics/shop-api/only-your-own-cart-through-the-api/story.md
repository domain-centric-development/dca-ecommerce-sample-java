---
id: only-your-own-cart-through-the-api
epic: shop-api
status: adopted
context: cart
feature: shopping
title: Only your own cart through the API
depends_on: [open-and-fill-a-cart-through-the-api]
---

# Only your own cart through the API

## Story

As a customer using a partner application I want nobody else to see or change my cart, not even learn that it
exists, so that what I collect stays mine; and as staff I want to see every cart in the shop.

## Acceptance criteria

<!-- happy-path: staff-list-every-cart -->

### Rule: Another customer's cart looks exactly like a cart that does not exist

#### reading-another-customers-cart-finds-nothing
- Given Ada's cart holds "Domain-Driven Design" × 2 and Grace holds her own customer token
- When Grace's client sends `GET /api/carts/<Ada's cartId>`
- Then the shop answers 404

#### adding-to-another-customers-cart-finds-nothing
- Given Ada's cart holds "Domain-Driven Design" × 2 and Grace holds her own customer token
- When Grace's client adds "Team Topologies" with quantity 1 to Ada's cart
- Then the shop answers 404
- And Ada still sees only "Domain-Driven Design" × 2 in her cart

#### removing-from-another-customers-cart-finds-nothing
- Given Ada's cart holds "Domain-Driven Design" × 2 and Grace holds her own customer token
- When Grace's client sends `DELETE /api/carts/<Ada's cartId>/items/<id of Domain-Driven Design>`
- Then the shop answers 404
- And Ada still sees "Domain-Driven Design" × 2 in her cart

#### another-customers-active-cart-finds-nothing
- Given Ada's `customerId` and Grace holding her own customer token
- When Grace's client sends `GET /api/carts/customer/<Ada's customerId>/active`
- Then the shop answers 404

### Rule: Only staff list every cart; a stranger is challenged, a customer is forbidden

#### staff-list-every-cart
- Given Ada's cart holds "Domain-Driven Design" × 2 and a client holding a staff token
- When it sends `GET /api/carts`
- Then the shop answers 200 with `carts`, one of which has Ada's `cartId`, her `customerId`, `status` "ACTIVE", `itemCount` 1, `total` 109.98 and `currency` "EUR"

#### a-stranger-cannot-list-carts
- Given a client without a token
- When it sends `GET /api/carts`
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`

#### a-customer-cannot-list-carts
- Given a client holding the customer token of Ada
- When it sends `GET /api/carts`
- Then the shop answers 403

### Rule: A call without a token is a throwaway guest

#### tokenless-calls-are-strangers-to-each-other
- Given a client that sends no token
- When it sends `POST /api/carts` twice
- Then both answers are 201, with two different `customerId`s

## Out of scope

- How a staff token comes into being — see `staff-create-a-product-through-the-api`.
- Refusals for products and quantities — `the-cart-api-refuses-what-the-cart`.
- Other people's carts on the pages and in the checkout — the cart and checkout epics.

## Notes

- Replay story: `API-07` (shop-api).
- Inventory: cart C-54, C-55 (through the API), C-57 (foreign cart), C-60 (tokenless caller), C-61, C-62 (foreign
  id); cross-cutting X11 (cart list), X12, X13.
