---
id: an-unusable-cart-request-is-a-problem-document
epic: shop-api
status: draft
context: cart
feature: shopping
title: A refused quantity is a problem document
depends_on: []
---

# A refused quantity is a problem document

## Story

As a partner application I want a refused quantity to answer in the same shape as every other refusal of the cart
API, so that one error handler reads all of them.

## Acceptance criteria

### Rule: A quantity is at least 1

#### a-quantity-of-zero-is-refused (happy path)
- Given Ada's open, empty cart
- When her client adds "Domain-Driven Design" with quantity 0
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
- And her cart is still empty

## Changed expectations

- A quantity of 0 is answered 400 now, with a body that is not a problem document, or one titled "One or more
  validation errors occurred." with a list of fields; after, with a problem document titled "Unacceptable value"
  without detail, as a malformed product id is.

## Assumptions

- open: "Unacceptable value" without detail (as written), or a detail that names the field?

## Notes

- From TODO #95 (a), validation half, checked 2026-10-15: still true in the Java shop; partly in the .NET shop (the
  framework's problem document with its own title).
- Spec (`cart/shopping`): changes `a-quantity-of-zero-is-refused`; holds `a-malformed-product-id-is-a-bad-request`.
