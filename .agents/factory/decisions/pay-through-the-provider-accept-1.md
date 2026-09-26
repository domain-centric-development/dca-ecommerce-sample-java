---
id: pay-through-the-provider-accept-1
story: pay-through-the-provider
stage: document
kind: acceptance
asked: 2026-09-26T06:49:33Z
digest: db729d139dc59f705da73ce1c64c84156b5811d6751378673810c60872f9280e
---

# Accept pay-through-the-provider?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- authorized-payment-moves-to-review: Given a checkout session at the payment step; And the payment provider authorizes payments; When the customer pays through the payment provider; Then the checkout shows the review step; And the payment provider received one payment request for the checkout session's total — `dev.domaincentric.sample.ecommerce.e2e.ProviderPaymentE2ETest#authorizedPaymentMovesTheCheckoutToReview`
- refused-payment-stays-at-payment: Given a checkout session at the payment step; And the payment provider refuses payments; When the customer pays through the payment provider; Then the checkout stays at the payment step; And the customer sees "The payment was refused. Please choose another way to pay." — `dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#refusedPaymentKeepsTheCustomerAtThePaymentStep`
- slow-provider-counts-as-unavailable: Given a checkout session at the payment step; And the payment provider answers only after 5 seconds; When the customer pays through the payment provider; Then the checkout stays at the payment step; And the customer sees "The payment provider is not available right now. Please try again later." — `dev.domaincentric.sample.ecommerce.checkout.ProviderPaymentIntegrationTest#slowProviderCountsAsUnavailable`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-09-26T07:39:58Z
