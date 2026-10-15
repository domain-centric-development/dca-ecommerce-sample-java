---
id: an-unknown-product-in-the-body-is-unprocessable
epic: shop-api
status: approved
context: cart
feature: shopping
title: A product the catalogue does not know, named in the body, is unprocessable
depends_on: []
---

# A product the catalogue does not know, named in the body, is unprocessable

## Story

As a partner application I want a request that names a product the catalogue does not know to be told apart from a
cart that is not there, so that I correct the product and do not look for the cart.

## Acceptance criteria

### Rule: A product the catalogue does not know cannot be added

#### an-unknown-product-is-not-available (happy path)
- Given Ada's open, empty cart
- When her client sends `POST /api/carts/<cartId>/items` with
  `{"productId":"00000000-0000-4000-8000-000000000001","quantity":1}`
- Then the shop answers 422 with an `application/problem+json` body
- And its title is "Article not available" and its detail
  "There is no article for product 00000000-0000-4000-8000-000000000001."
- And her cart is still empty

## Out of scope

- A product id that is not even a UUID: answered 400, as before (`a-malformed-product-id-is-a-bad-request`).
- A product named in the address, as when a line is removed: the address names the resource, so a product the cart
  does not hold stays 404.

## Changed expectations

- Adding a product the catalogue does not know through the API is answered 404 now, as if the cart were not there,
  and the spec's example names a product id that is not even a UUID, which is answered 400; after, a well-formed
  product id the catalogue does not know is answered 422 Unprocessable Content — the cart is there, the product the
  body names is not.

## Assumptions

- answered: 404 only for the resource the address names; a well-formed but unknown reference in the body is 422
  Unprocessable Content; one that does not parse is 400 (planning/questions-2026-10-15.md Q3)

## Notes

- From the 2026-10-15 questions (Q3): the scenario kept its example `no-such-product`, which
  `a-malformed-product-id-is-a-bad-request` answers 400 — the spec contradicted itself.
- Other references in a request body, checked 2026-10-15 in both shops' APIs: none answered 404. Adding to a cart is
  the only request whose body names another resource; removing a line names the product in the address, and product
  creation, login and registration name no other resource. So this story carries the one key.
- Spec (`cart/shopping`): changes `an-unknown-product-is-not-available`; holds `a-malformed-product-id-is-a-bad-request`
  and `removing-a-product-not-in-the-cart`.
