---
id: each-payment-names-its-checkout
epic: provider-payment
status: approved
context: checkout
feature: payment
title: Each payment request names the checkout it pays for
depends_on: []
---

# Each payment request names the checkout it pays for

## Story

As the shop's operator I want every payment request to the provider to carry a reference of the checkout it pays
for, so that the provider and the shop can match a payment to its order, a repeated request is recognised as the same
payment, and a provider's answer for one checkout never touches another.

## Acceptance criteria

### Rule: A payment request carries the reference of its checkout

#### the-payment-request-names-its-checkout (happy path)
- Given a checkout session at the payment step
- And the payment provider authorizes payments
- When the customer pays through the payment provider
- Then the payment provider received one payment request for the checkout session's total
- And that request carries the checkout reference, the checkout session's id as text, as `merchantReference`

#### two-checkouts-send-two-references
- Given two customers, each with a checkout session at the payment step
- When both pay through the payment provider
- Then the payment provider received two payment requests with two different `merchantReference` values

#### the-provider-answers-per-checkout
- Given two customers, each with a checkout session at the payment step
- And the payment provider refuses the payment for the first customer's checkout reference only
- When both pay through the payment provider
- Then the first customer stays at the payment step with "The payment was refused. Please choose another way to pay."
- And the second customer's checkout shows the review step

#### a-second-attempt-names-the-same-checkout
- Given a checkout session at the payment step
- And the customer's first payment for it was refused by the payment provider
- When the customer pays through the payment provider again
- Then the payment provider received two payment requests for that checkout session
- And both carry the same `merchantReference`

## Assumptions

- answered: the provider contract carries the reference — `merchantReference`, a string, the checkout session's id as
  text, the same in every payment request of that checkout; written into the technical description's `## Integrations`
  (user, 2026-10-14)
- answered: the reference the shop sends is the *checkout reference* (field `merchantReference`), kept apart from the
  *payment reference* the provider answers with and the *order reference* (user, 2026-10-14)
- answered: a second attempt of the same checkout carries the same reference — its own scenario (user, 2026-10-14)

## Changed expectations

- `POST /payments` carries `{ amount, currency }` now and `{ amount, currency, merchantReference }` after; the
  reference is the checkout session's id as text, the same for every request of that checkout.

## Out of scope

- An idempotent provider (answering a repeated reference with the first answer): the stub and the provider contract
  only carry the reference here.

## Notes

- Asked by the user 2026-10-14 (WP-108): with a reference per checkout, the test suites' payment stub arranges its
  answer per checkout instead of for the whole suite, so the payment tests no longer need to run alone
  (`ProviderPaymentE2ETest` @Isolated / `PaymentProviderCollection` DisableParallelization) and stop influencing each
  other under parallel load (TODO #140).
- Draft until the running WP-108 story queue is through (user 2026-10-14: "after the running 15").
