---
id: the-back-office-lists-low-stock
epic: stock-oversight
status: draft
context: inventory
title: The back office lists the products that run low
depends_on: []
---

# The back office lists the products that run low

## Story

As an operator I want a back-office page that lists the products whose stock has fallen below a threshold, so that I
can reorder them before a customer finds them sold out.

## Acceptance criteria

### Rule: The low-stock page lists every product whose available quantity is below the threshold, with that quantity

#### the-low-stock-page-lists-what-runs-low (happy path)
- Given the operator is logged in to the back office
- And the seeded "Wooden Hexagon Desk Model" with a stock of 8, and every other product with 10 or more
- When they open the low-stock page with the threshold 10
- Then it lists "Wooden Hexagon Desk Model" with "8"
- And no other product

#### a-product-at-the-threshold-is-not-listed
- Given the operator is logged in to the back office
- And the seeded "Wooden Hexagon Desk Model" with a stock of 8
- When they open the low-stock page with the threshold 8
- Then it does not list "Wooden Hexagon Desk Model"

#### nothing-runs-low
- Given the operator is logged in to the back office and every product has a stock of 1 or more
- When they open the low-stock page with the threshold 1
- Then it says that no product runs low

## Changed expectations

- The low-stock overview is answered only to tests now; no operator can reach it. After, the back office shows it.

## Assumptions

- open: does the shop want this page at all — or is the overview recorded as not part of today's shop (then this
  draft is withdrawn, and the overview's use case with it)?
- open: the page's address, heading and place in the back office's navigation, and the sentence for "nothing runs
  low"?
- open: the threshold — one the operator types on the page (as written), with which default, or a reorder level per
  product?
- open: should reserved quantity count towards the available quantity, or is available quantity alone the measure
  (STORY-P1's open question)?
- open: the back office reads only the application's own infrastructure today; a page that lists stock reads the
  inventory — is that a new relationship on the designed context map, or a page of the inventory under the back
  office's address?

## Notes

- From TODO #96, checked 2026-10-15: still true in both shops — the overview exists as a use case only, called by
  tests; no page, API route or tool reaches it. `STORY-P1` delivered it without a surface and with two open
  assumptions.
- Spec (`inventory`): adds `the-low-stock-page-lists-what-runs-low`, `a-product-at-the-threshold-is-not-listed` and
  `nothing-runs-low`; the overview's existing criteria (`lists-products-below-the-threshold`,
  `excludes-products-at-or-above-the-threshold`, `names-the-available-quantity`, `empty-answer-when-nothing-is-low`)
  are the plan's to hold or replace.
