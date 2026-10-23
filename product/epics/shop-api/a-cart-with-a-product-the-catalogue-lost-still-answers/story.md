---
id: a-cart-with-a-product-the-catalogue-lost-still-answers
epic: shop-api
status: draft
created: 2026-10-23T21:25:20Z
context: cart
feature: shopping
title: A cart with a product the catalogue lost still answers
closes: [every-cart-answer-shows-todays-price#1, the-cart-oversight-totals-at-todays-prices#1]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# A cart with a product the catalogue lost still answers

## Story

As a customer or a member of staff I want a cart to answer although one of its products lost its catalogue data, so that one missing product does not turn every answer into a bad request.

## Acceptance criteria

### Rule: One product the catalogue lost does not fail the cart's answer

#### a-cart-answers-with-a-product-the-catalogue-lost (happy path)
- Given a cart holding "Clean Architecture" once and "Domain-Driven Design" once
- And the catalogue no longer knows "Domain-Driven Design"
- When "Clean Architecture" is added once more through the API
- Then the answer is 200 and the cart holds "Clean Architecture" with quantity 2

#### the-staff-list-answers-with-a-product-the-catalogue-lost
- Given the cart above
- When a member of staff reads the list of every cart
- Then the answer is 200 and lists the cart

## Assumptions

- open: what a line shows whose product the catalogue no longer knows

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- Java answers "bad request" for a saved add or remove and for the whole staff list.
