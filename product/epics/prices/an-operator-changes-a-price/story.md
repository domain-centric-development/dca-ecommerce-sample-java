---
id: an-operator-changes-a-price
epic: prices
status: approved
context: pricing
title: An operator changes a product's price
depends_on: []
---

# An operator changes a product's price

## Story

As an operator who looks after prices I want to change a product's price through the staff API, so that the shop
sells at the price the owner decided and every page shows it.

## Acceptance criteria

### Rule: A changed price is the price every page shows from then on

#### a-changed-price-shows-on-the-product-page (happy path)
- Given a client holding a staff token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":49.99}`
- Then the product page of "Domain-Driven Design" shows "49.99 EUR"
- And its catalogue card shows "49.99 EUR"

### Rule: Only staff change a price; a stranger is challenged, a customer is forbidden

#### a-stranger-cannot-change-a-price
- Given a client without a token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":49.99}`
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"

#### a-customer-cannot-change-a-price
- Given a client holding the customer token of Ada, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":49.99}`
- Then the shop answers 403
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"

### Rule: A new price is greater than zero

#### a-new-price-of-zero-is-refused
- Given a client holding a staff token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":0}`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `price` with the reason
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"

#### a-new-price-of-one-cent-is-accepted
- Given a client holding a staff token
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":0.01}`
- Then the product page of "Domain-Driven Design" shows "0.01 EUR"

### Rule: The price address names a product of the catalogue by its id

#### changing-the-price-of-an-unknown-product-finds-nothing
- Given a client holding a staff token, and no product with the id "00000000-0000-4000-8000-000000000001"
- When it sends `PUT /api/products/00000000-0000-4000-8000-000000000001/price` with `{"price":49.99}`
- Then the shop answers 404

#### a-malformed-product-id-in-the-price-address-is-a-bad-request
- Given a client holding a staff token
- When it sends `PUT /api/products/no-such-product/price` with `{"price":49.99}`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value"

## Out of scope

- A back-office page for prices: not built (planning/questions-2026-10-15.md Q4.7 — no further pages for now).
- What a cart shows after a change: story `the-cart-shows-a-changed-price`.
- What an open checkout shows after a change: story `an-open-checkout-keeps-its-price`.
- The highest price and the rounding to cents: a new price is held to them as a price at creation is; they are not
  repeated here.

## Changed expectations

- A price can be changed only by tests now; no operator can reach it. After, an operator changes it through the API
  with a staff token.

## Assumptions

- answered: a price change is wanted, through the staff API, with no page (planning/questions-2026-10-15.md Q4.7)
- answered: a cart shows today's price after a change, an open checkout keeps the price it started with
  (planning/questions-2026-10-15.md Q4.7) — two stories of their own, one per context

## Notes

- From TODO #96, checked 2026-10-15: still true in both shops — no use case changes a price; the price owner's model
  can, but only the price set up for a new product reaches it, and only tests drive a change.
- The address `PUT /api/products/<id>/price` and the body `{"price": <number>}` follow the staff API's product
  creation, which takes the price as a number; the product is addressed in the path, so an unknown one is 404 and a
  malformed one 400 (planning/questions-2026-10-15.md Q3). The rules of a new price are those `product/assortment`
  holds for a price at creation.
- The story stays in the epic `prices`: a changed price shown the same everywhere is that epic's goal.
- Spec (`pricing`): adds the seven keys above; holds `page-and-card-agree`.
