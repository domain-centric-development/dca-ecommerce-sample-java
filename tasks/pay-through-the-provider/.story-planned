---
id: pay-through-the-provider
epic: provider-payment
status: approved
context: checkout
title: Pay through the payment provider
depends_on: []
---

# Pay through the payment provider

## Story

As a customer I want my payment at checkout to go through the payment provider so that my order is
paid for real, and I learn at once when the provider refuses the payment or cannot be reached.

## Acceptance criteria

### Rule: A payment the provider authorizes moves the checkout on

#### authorized-payment-moves-to-review (happy path)
- Given a checkout session at the payment step
- And the payment provider authorizes payments
- When the customer pays through the payment provider
- Then the checkout shows the review step
- And the payment provider received one payment request for the checkout session's total

### Rule: A refused payment keeps the customer at the payment step

#### refused-payment-stays-at-payment
- Given a checkout session at the payment step
- And the payment provider refuses payments
- When the customer pays through the payment provider
- Then the checkout stays at the payment step
- And the customer sees "The payment was refused. Please choose another way to pay."

### Rule: A provider that does not answer in time counts as unavailable

#### slow-provider-counts-as-unavailable
- Given a checkout session at the payment step
- And the payment provider answers only after 5 seconds
- When the customer pays through the payment provider
- Then the checkout stays at the payment step
- And the customer sees "The payment provider is not available right now. Please try again later."

## Out of scope

- The provider's webhook for asynchronous confirmations — a later story.
- Refunds and cancellations through the provider.

## Assumptions

- answered: the provider's contract — the request, the answers and what counts as unavailable — is the one `project/tech.md` names under `## Integrations`.
- answered: where the provider's address is not configured, the shop keeps the stand-in inside the sample, so running the shop locally needs no provider.
