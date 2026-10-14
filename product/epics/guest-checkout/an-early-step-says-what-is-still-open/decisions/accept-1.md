---
id: an-early-step-says-what-is-still-open-accept-1
story: an-early-step-says-what-is-still-open
stage: document
kind: acceptance
asked: 2026-10-13T22:00:04Z
digest: db068e334c904434329ab5bb3d9722f5b390e842a26c64ab0a45ffd1947a5558
---

# Accept an-early-step-says-what-is-still-open?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- delivery-sent-before-the-buyer-step: Given a guest who has just started the checkout and has not submitted the buyer step; When a delivery form with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country; Then the guest ends on the buyer step at `/checkout/buyer`; And the page says "Please complete the previous checkout steps first."; And the review cannot be opened afterwards: `/checkout/review` opens the buyer step — `dev.domaincentric.sample.ecommerce.e2e.StepsShowTheProgressE2ETest#aDeliveryFormSentBeforeTheBuyerStepSaysThatThePreviousStepsComeFirst`
- place-order-before-the-review: Given a guest who has submitted the buyer and delivery steps and is on the payment step; When a "Place Order" is sent to the shop; Then the guest ends on the payment step at `/checkout/payment`; And the page says "Please complete the previous checkout steps first."; And no confirmation is shown: the order is not placed — `dev.domaincentric.sample.ecommerce.checkout.StepsShowTheProgressIntegrationTest#placeOrderSentFromThePaymentStepEndsOnThePaymentStep`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-14T04:54:18Z
