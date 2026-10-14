---
id: an-operator-changes-a-price
epic: prices
status: draft
context: pricing
title: An operator changes a product's price
depends_on: []
---

# An operator changes a product's price

## Story

As an operator who looks after prices I want to change a product's price, so that the shop sells at the price the
owner decided and every page shows it.

## Acceptance criteria

### Rule: A changed price is the price every page shows from then on

#### a-changed-price-shows-on-the-product-page (happy path)
- Given the operator is logged in and "Domain-Driven Design" costs 54.99 EUR
- When the operator sets the price of "Domain-Driven Design" to 49.99 EUR
- Then its product page shows "49.99 EUR"
- And its catalogue card shows "49.99 EUR"

## Changed expectations

- A price can be changed only by tests now; no operator can reach it. After, an operator changes it.

## Assumptions

- open: does the shop want this at all — or is changing a price recorded as not part of today's shop (then this draft
  is withdrawn)?
- open: through which surface — a back-office page, or the API with the staff token that creates products — and who
  may use it?
- open: the rules of a new price — the same as at creation (greater than zero, at most 999999999999.99, rounded half
  up to two decimals), each then a pair of scenarios?
- open: what a cart and an open checkout that hold the product show after the change (the cart page has a
  price-change notice that no scenario names yet)?
- open: this epic is about one price on every page; does an operator's price change belong here, or in an epic of its
  own with its own intent and outcome event?

## Notes

- From TODO #96, checked 2026-10-15: still true in both shops — no use case changes a price; the price owner's model
  can, but only the price set up for a new product reaches it, and only tests drive a change.
- Spec (`pricing`): adds `a-changed-price-shows-on-the-product-page`; holds `page-and-card-agree`.
