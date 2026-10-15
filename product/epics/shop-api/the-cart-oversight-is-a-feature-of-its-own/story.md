---
id: the-cart-oversight-is-a-feature-of-its-own
epic: shop-api
status: adopted
context: cart
feature: cartoversight
title: The staff list of every cart is the feature Cart oversight
depends_on: [the-cart-oversight-has-its-own-place-in-the-code]
---

# The staff list of every cart is the feature Cart oversight

## Story

As staff who look after the shop I want the list of every cart to be a feature of its own, Cart oversight, so that
what holds for it is found under its own name and not among the shopper's cart.

## Acceptance criteria

### Rule: Only staff list every cart; a stranger is challenged, a customer is forbidden

#### staff-list-every-cart (happy path)
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

## Assumptions

- answered: the staff list of every cart is the feature Cart oversight (`cartoversight`, "Warenkorb-Übersicht"), with
  its own spec folder; no behaviour changes (planning/questions-2026-10-15.md Q2)
- answered: Cart oversight (cartoversight): the staff's view across every cart of the shop — whose each cart is, its
  state and its total (planning/questions-2026-10-15.md Q2) — the glossary entry, `**Type:** Feature`

## Notes

- A move with no visible change. The backlog skill has no kind for it: a task (`kind: task`) changes no spec, and a
  story's criteria must be red before the build. So the code's half is the task
  `the-cart-oversight-has-its-own-place-in-the-code`, and this story carries the moved criteria word for word, held,
  as `status: adopted` — the adoption maps them to the tests that prove them today and moves them in the spec.
- Spec (`cart`): moves `staff-list-every-cart`, `a-stranger-cannot-list-carts` and `a-customer-cannot-list-carts`:
  shopping → cartoversight; adds feature `cartoversight`.
