---
id: a-refusal-after-the-authorization-cancels-it
epic: provider-payment
status: approved
created: 2026-10-23T21:25:20Z
context: checkout
feature: payment
title: A refusal after the authorization cancels it
closes: [the-payment-is-authorized-at-place-order#3]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# A refusal after the authorization cancels it

## Story

As a buyer whose order is refused after the provider authorized the payment I want the authorization cancelled, so that no money stays held for an order that does not exist.

## Acceptance criteria

### Rule: An order refused after the authorization leaves no payment held

#### a-refused-order-cancels-its-authorization (happy path)
- Given a customer on the review of "Clean Architecture" at 39.99 EUR, paying with a provider that authorizes payments
- And while the provider authorizes the 39.99 EUR, the checkout is replaced by a new checkout from the same cart
- When the customer presses "Place Order" on the first checkout's review
- Then the order is not placed and the review stays open with the refusal it shows today
- And the provider receives `POST /payments/{payment reference}/cancel` for the payment it authorized

## Changed expectations

- Now: when the order is refused after the provider authorized the payment, only the reserved stock is given back; the payment stays authorized at the provider. After: the shop also asks the provider to cancel that authorization. What the customer sees does not change.

## Assumptions

- answered: when the cancellation itself fails (no `204`, no answer within 2 seconds), the customer sees the same refusal as without it, and the shop logs a warning naming the payment reference; no second attempt (the user, 2026-10-24). Today's refusal already looks so, so this is a guard for the plan, not a criterion.

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- TODO #167: both shops release the stock on a refusal after the authorization, neither cancels the authorization (`POST /payments/{reference}/cancel` exists). Reachable with a database in a transaction.
- The replacement happens during the provider's answer, a race a browser cannot bring about on purpose; the plan decides how the end-to-end test provokes it.
- Closes the-payment-is-authorized-at-place-order#3 too (the user, 2026-10-24): `PaymentProvider.cancelPayment` has had no caller since the payment moved to Place Order; this story gives it one. Moved here from `the-checkout-code-findings-are-closed`.
- Taken over from the .NET sample's release (2026-10-24). This shop's checkout glossary has no "Payment cancellation" entry, so the .NET assumption about rewriting it does not apply here.
