---
id: the-cart-oversight-has-its-own-place-in-the-code
epic: shop-api
kind: task
status: approved
context: cart
title: The cart oversight has its own place in the code
closes: []
depends_on: []
---

# The cart oversight has its own place in the code

The staff's list of every cart leaves the technical `operations` group of the cart's application layer and becomes
the feature Cart oversight (`cartoversight`, "Warenkorb-Übersicht"): its use case moves into the feature's own
application folder, and the route `GET /api/carts` moves out of `ShoppingCartResource` into a controller of its own,
`CartOversightResource`. The context's glossary names the feature in its features table and under `## Features`
(**Type:** Feature) in place of `operations`. No caller sees a difference: the address, the answers and who may call
it stay as they are.

A change no actor sees: no acceptance criterion, no test stage. It runs plan, build, tidy, judge and document, and
the existing suite, run whole, is its evidence; a test that existed before it stays as it is. Behaviour that changes
is a story with a red test, not a task.

## Notes

- The user's decision of 2026-10-15 (planning/questions-2026-10-15.md Q2, W2, from TODO #146): own feature
  `cartoversight`, own folder, own controller, own spec folder, through a small story in both shops.
- The spec's half — the staff list's three criteria moving from `cart/shopping` to `cart/cartoversight` — is the
  adopted story `the-cart-oversight-is-a-feature-of-its-own`, which follows this task: a task changes no spec, and a
  story whose criteria all hold already is adopted, never built.
