---
id: a-refusal-after-the-authorization-cancels-it
epic: provider-payment
status: draft
created: 2026-10-23T21:25:20Z
context: checkout
feature: payment
title: A refusal after the authorization cancels it
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# A refusal after the authorization cancels it

## Story

As a buyer whose order is refused after the provider authorized the payment I want the authorization cancelled, so that no money stays held for an order that does not exist.

## Acceptance criteria

### Rule: An order refused after the authorization leaves no payment held

#### a-refused-order-cancels-its-authorization (happy path)
- Given a buyer on the review whose payment the provider authorizes
- And the checkout was replaced by a new checkout from the same cart before the order is confirmed
- When the buyer presses "Place Order"
- Then the order is refused
- And the provider is asked to cancel the authorization

## Assumptions

- open: what the buyer sees when the cancel itself fails

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- TODO #167: both shops release the stock on a refusal after the authorization, neither cancels the authorization (`POST /payments/{reference}/cancel` exists). Reachable with a database in a transaction.
