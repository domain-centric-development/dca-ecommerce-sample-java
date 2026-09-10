---
id: STORY-P1
epic: stock-oversight
status: approved
context: Inventory
title: Low stock overview
depends_on: []
---

# Low stock overview

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

- open: Should reserved quantity count towards the available quantity for this overview, or is
  available quantity alone the right measure?
- open: Is one threshold for all products the right shape, or does a product carry its own reorder
  level?
