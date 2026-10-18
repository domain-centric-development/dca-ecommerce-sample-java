---
id: STORY-P1
epic: stock-oversight
status: superseded
delivered: 2026-09-10T20:14:35Z
delivered_by: hand:unknown
created: 2026-10-11T11:59:24Z
context: Inventory
title: Low stock overview
depends_on: []
---

# Low stock overview

## Superseded

- Withdrawn: the low-stock overview, delivered without a surface, is not part of the shop, and its unused capability
  leaves the code; the user's decision of 2026-10-15 (planning/questions-2026-10-15.md Q4.7) — no further pages for now, first the current
  state stable and correct.

## Story

As an operator I want to see the products whose available quantity has fallen below a threshold I
give, so that I can reorder them before they run out.

## Acceptance criteria

- lists-products-below-the-threshold: Asking for the products below a threshold returns every
  product whose available quantity is lower than that threshold.
- excludes-products-at-or-above-the-threshold: A product whose available quantity equals or exceeds
  the threshold is not part of the answer.
- names-the-available-quantity: Each product in the answer names its available quantity, so the
  operator can tell how urgent it is.
- empty-answer-when-nothing-is-low: When no product is below the threshold, the answer is empty and
  not an error.

## Assumptions

- answered: no longer asked — the overview is withdrawn, so neither the measure nor the threshold is decided
  (planning/questions-2026-10-15.md Q4.7)
