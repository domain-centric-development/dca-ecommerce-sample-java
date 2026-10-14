---
id: paying-again-requests-no-second-payment
epic: provider-payment
status: draft
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

### Rule: A checkout whose payment the provider authorized holds that one payment

#### paying-again-sends-no-second-request (happy path)
- Given a checkout session whose payment the payment provider authorized, and the customer at the review
- When the customer goes back to the payment step and pays through the payment provider again
- Then the checkout shows the review step
- And the payment provider received one payment request for that checkout session

## Changed expectations

- Paying again from the payment step after the payment was authorized sends a second payment request now, and the
  first stays open at the provider; after, no second request is sent while the first payment stands.

## Assumptions

- open: which behaviour is wanted — (a) the authorized payment stands and paying again sends nothing (as written),
  (b) the first payment is released at the provider and a new one requested (the provider contract in the technical
  description knows no release yet — it would have to be added there first), or (c) once the payment is authorized
  the review no longer leads back to the payment step?
- open: under (a), what the customer sees when they pay again — the review as written, or a message, and which?

## Notes

- From TODO #94 (g), checked 2026-10-15: still true in both shops — a second payment at the payment step requests a
  new authorization and overwrites the first, which is never released.
- Depends on `each-payment-names-its-checkout` (approved in the Java shop, delivered in the .NET shop): both requests
  then carry the same checkout reference, which is how the provider's side tells them apart in the test.
- Spec (`checkout/payment`): adds `paying-again-sends-no-second-request`; holds `authorized-payment-moves-to-review`
  and `choosing-the-provider-opens-the-review`.
