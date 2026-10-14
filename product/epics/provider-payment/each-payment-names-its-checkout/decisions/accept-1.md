---
id: each-payment-names-its-checkout-accept-1
story: each-payment-names-its-checkout
stage: document
kind: acceptance
asked: 2026-10-14T22:49:55Z
digest: 82a0ee90217db7813e0e8a1e3aae8d3b56bc7232c0649e5e87f6b2f2ff9872dd
---

# Accept each-payment-names-its-checkout?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-payment-request-names-its-checkout: Given a checkout session at the payment step; And the payment provider authorizes payments; When the customer pays through the payment provider; Then the payment provider received one payment request for the checkout session's total; And that request carries the checkout reference, the checkout session's id as text, as `merchantReference` — `dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#thePaymentRequestCarriesTheCheckoutReference`
- two-checkouts-send-two-references: Given two customers, each with a checkout session at the payment step; When both pay through the payment provider; Then the payment provider received two payment requests with two different `merchantReference` values — `dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#twoCheckoutsSendTwoDifferentReferences`
- the-provider-answers-per-checkout: Given two customers, each with a checkout session at the payment step; And the payment provider refuses the payment for the first customer's checkout reference only; When both pay through the payment provider; Then the first customer stays at the payment step with "The payment was refused. Please choose another way to pay."; And the second customer's checkout shows the review step — `dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#theProvidersAnswerForOneCheckoutTouchesNoOther`
- a-second-attempt-names-the-same-checkout: Given a checkout session at the payment step; And the customer's first payment for it was refused by the payment provider; When the customer pays through the payment provider again; Then the payment provider received two payment requests for that checkout session; And both carry the same `merchantReference` — `dev.domaincentric.sample.ecommerce.checkout.CheckoutReferenceIntegrationTest#aSecondAttemptCarriesTheSameReference`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-14T23:27:33Z
rationale: Accepted on the person's behalf by the managing session (authorized 2026-10-14 21:35 for the remaining WP-108 queue).
