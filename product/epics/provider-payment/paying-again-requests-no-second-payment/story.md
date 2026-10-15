---
id: paying-again-requests-no-second-payment
epic: provider-payment
status: approved
context: checkout
feature: payment
title: Paying again for a checkout leaves no second payment open
depends_on: [each-payment-names-its-checkout]
---

# Paying again for a checkout leaves no second payment open

## Story

As a customer who goes back from the review to the payment step and pays again I want only one payment to stand for
my order, so that the provider never holds two payments for one checkout.

## Acceptance criteria

### Rule: A checkout whose authorized payment covers its total keeps that payment

#### paying-again-for-the-same-amount-keeps-the-payment (happy path)
- Given a checkout session whose payment of its total the payment provider authorized
- And the customer went back from the review to the payment step
- When the customer pays through the payment provider again
- Then the checkout shows the review step and the message "Your payment is already authorized."
- And the payment provider received one payment request for that checkout session

### Rule: A checkout whose total changed after its payment was authorized cancels that payment and requests a new one

#### paying-again-for-a-changed-amount-replaces-the-payment
- Given a checkout session with "Domain-Driven Design" once and "Standard Shipping", whose payment of "59.98" the
  payment provider authorized
- And the customer went back from the review to the delivery step and chose "Express Shipping"
- When the customer pays through the payment provider again
- Then the payment provider received the cancellation of the first payment
- And a second payment request of "64.98" with the same `merchantReference` as the first
- And the checkout shows the review step

## Out of scope

- A cancellation the provider refuses or does not answer: the answers name no behaviour for it yet.

## Changed expectations

- Paying again from the payment step after the payment was authorized sends a second payment request now, and the
  first stays open at the provider; after, the same amount sends nothing and says "Your payment is already
  authorized.", and a changed amount cancels the first payment before it requests the new one.

## Assumptions

- answered: the same amount keeps the authorization — no new request, the customer sees "Your payment is already
  authorized." and the review; a changed amount cancels the old authorization at the provider and requests a new one
  with the same `merchantReference`; the provider contract gains a cancel call (planning/questions-2026-10-15.md
  Q4.5)

## Notes

- From TODO #94 (g), checked 2026-10-15: still true in both shops — a second payment at the payment step requests a
  new authorization and overwrites the first, which is never released.
- The cancel call is in the technical description's `## Integrations` (`POST /payments/{payment reference}/cancel`).
- Depends on `each-payment-names-its-checkout` (delivered in both shops): every request of a checkout carries the
  same checkout reference.
- Spec (`checkout/payment`): adds `paying-again-for-the-same-amount-keeps-the-payment` and
  `paying-again-for-a-changed-amount-replaces-the-payment`; holds `authorized-payment-moves-to-review`,
  `a-second-attempt-names-the-same-checkout` and `choosing-the-provider-opens-the-review`.
