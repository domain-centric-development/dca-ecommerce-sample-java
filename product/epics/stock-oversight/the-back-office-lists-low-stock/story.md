---
id: the-back-office-lists-low-stock
epic: stock-oversight
status: superseded
created: 2026-10-14T23:44:13Z
context: inventory
title: The back office lists the products that run low
depends_on: []
---

# The back office lists the products that run low

## Superseded

- Not built: the low-stock page is not part of the shop, and the unused low-stock capability leaves the code; the
  user's decision of 2026-10-15 (planning/questions-2026-10-15.md Q4.7) — no further pages for now, first the current
  state stable and correct.

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

- answered: the low-stock page is not wanted; the draft is superseded (planning/questions-2026-10-15.md Q4.7)

## Notes

- From TODO #96, checked 2026-10-15: still true in both shops — the overview exists as a use case only, called by
  tests; no page, API route or tool reaches it. `STORY-P1` delivered it without a surface and with two open
  assumptions.
- Spec (`inventory`): adds `the-low-stock-page-lists-what-runs-low`, `a-product-at-the-threshold-is-not-listed` and
  `nothing-runs-low`; the overview's existing criteria (`lists-products-below-the-threshold`,
  `excludes-products-at-or-above-the-threshold`, `names-the-available-quantity`, `empty-answer-when-nothing-is-low`)
  are the plan's to hold or replace.
