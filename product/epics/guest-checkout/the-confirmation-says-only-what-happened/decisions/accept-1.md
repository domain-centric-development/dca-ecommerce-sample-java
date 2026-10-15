---
id: the-confirmation-says-only-what-happened-accept-1
story: the-confirmation-says-only-what-happened
stage: document
kind: acceptance
asked: 2026-10-15T09:26:49Z
digest: 120f94589cf32ab374bfa804854a2e4c0b1e48dd9a7c6bc25d8ad3c8cc60892e
---

# Accept the-confirmation-says-only-what-happened?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-confirmation-promises-no-email: Given the guest "jane@example.com" is on the review step; When the guest presses "Place Order"; Then the confirmation page does not say "We've sent a confirmation email"; And it does not say "You can track your order status using your order reference." — `dev.domaincentric.sample.ecommerce.e2e.PlaceTheOrderE2ETest#theConfirmationPromisesNeitherAnEmailNorOrderTracking`
- the-confirmation-shows-the-order-total: Given the guest is on the review step with "Domain-Driven Design" once and "Standard Shipping"; When the guest presses "Place Order"; Then the confirmation page shows "Order Total" "59.98 EUR"; And it does not say "Total Paid" — `dev.domaincentric.sample.ecommerce.checkout.PlaceTheOrderIntegrationTest#theConfirmationCallsTheAmountTheOrderTotal`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-15T14:56:00Z
rationale: Accepted by the person (2026-10-15), recorded by the managing session.
