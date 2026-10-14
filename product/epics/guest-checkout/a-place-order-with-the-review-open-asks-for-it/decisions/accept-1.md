---
id: a-place-order-with-the-review-open-asks-for-it-accept-1
story: a-place-order-with-the-review-open-asks-for-it
stage: document
kind: acceptance
asked: 2026-10-14T17:41:38Z
digest: 340e1f80c8d3ad8d1d47364c005356c3dc26b0ff947fd47626a3c54c9bb5db00
---

# Accept a-place-order-with-the-review-open-asks-for-it?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- place-order-with-the-review-open-asks-for-the-review: Given a guest whose review step is open to them, but who has not placed the order from it; When a "Place Order" is sent to the shop from another step page than the review; Then the guest ends on the review step at `/checkout/review`; And the page says "Please review your order before placing it."; And no confirmation is shown: the order is not placed — `dev.domaincentric.sample.ecommerce.e2e.StepsShowTheProgressE2ETest#aPlaceOrderSentFromThePaymentStepWithTheReviewOpenLeadsToTheReview`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-14T17:57:12Z
