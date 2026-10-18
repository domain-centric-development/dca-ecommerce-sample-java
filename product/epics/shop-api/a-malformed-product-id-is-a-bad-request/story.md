---
id: a-malformed-product-id-is-a-bad-request
epic: shop-api
status: delivered
delivered: 2026-10-14T23:02:46Z
delivered_by: gate
created: 2026-10-14T15:12:58Z
context: cart
feature: shopping
title: The cart API answers a malformed product id as a malformed call
depends_on: []
---

# The cart API answers a malformed product id as a malformed call

## Story

As a partner application I want the cart API to tell a product id that is not even well-formed apart from one no
product has, so that a bug in my request reads as a bad request and not as a product that is gone.

## Acceptance criteria

### Rule: A product id is a UUID; a product id that does not parse is a malformed call

#### a-malformed-product-id-is-a-bad-request (happy path)
- Given Ada's open, empty cart
- When her client sends `POST /api/carts/<cartId>/items` with `{"productId":"no-such-product","quantity":1}`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
- And her cart is still empty

#### removing-a-malformed-product-id-is-a-bad-request
- Given Ada's cart holds "Domain-Driven Design" × 1
- When her client sends `DELETE /api/carts/<cartId>/items/no-such-product`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
- And her cart still holds "Domain-Driven Design" × 1

## Out of scope

- A cart id that does not parse: the cart routes keep answering it as a cart that does not exist for the caller.
- The product pages: a product address that does not parse keeps showing the not-found page.

## Changed expectations

- A product id that is not a UUID, such as "no-such-product", is answered 404 "Article not available" when it is
  added to a cart now, and 400 "Unacceptable value" without detail after.
- An unknown product is still answered 404 "Article not available" with "There is no article for product …."; the
  example of it is a well-formed id no product has, "00000000-0000-4000-8000-000000000001", instead of
  "no-such-product".
- Removing a product whose id is not a UUID is answered 400 "Unacceptable value" without detail after, whatever it is
  answered now.

## Notes

- Decided by the user 2026-10-14 (triage Gruppe 9): input that does not parse into the id is a malformed call, not
  the failure of an unknown product — the guide's rule for named failures. The adapter that receives the request
  parses the id; the failure of an unknown product carries the product id, never the caller's text.

## Assumptions

- answered: SharedScenariosTest also knows the titles of the stories not yet delivered (their `####` criteria, title = Title line else the key in words); the spec stays the only source for the Test-line and duplicate checks. Fixed on main in d5ce1e9; nothing changes in this story — run the stage again as it is. (a-malformed-product-id-is-a-bad-request-01)
- answered: Commit the three scenarios' spec merge on main, then integrate main into this story's branch (or let the runner (a-malformed-product-id-is-a-bad-request-02)
