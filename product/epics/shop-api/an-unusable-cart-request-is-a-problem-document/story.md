---
id: an-unusable-cart-request-is-a-problem-document
epic: shop-api
status: approved
created: 2026-10-14T23:44:13Z
context: cart
feature: shopping
title: A refused quantity is a problem document
depends_on: [an-unknown-product-in-the-body-is-unprocessable]
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
- And its title is "Unacceptable value" and its `errors` names `quantity` with the reason
- And her cart is still empty

## Changed expectations

- A quantity of 0 is answered 400 now, with a body that is not a problem document, or one titled "One or more
  validation errors occurred." with a list of fields; after, with a problem document titled "Unacceptable value"
  whose `errors` names each refused field and why.

## Assumptions

- answered: a refused value is a problem document titled "Unacceptable value" with an `errors` map that names each
  field and why (planning/questions-2026-10-15.md Q4.6)

## Notes

- From TODO #95 (a), validation half, checked 2026-10-15: still true in the Java shop; partly in the .NET shop (the
  framework's problem document with its own title).
- Depends on `an-unknown-product-in-the-body-is-unprocessable`: both change the cart API's refusals, one after the
  other.
- Spec (`cart/shopping`): changes `a-quantity-of-zero-is-refused`; holds `a-malformed-product-id-is-a-bad-request`.
