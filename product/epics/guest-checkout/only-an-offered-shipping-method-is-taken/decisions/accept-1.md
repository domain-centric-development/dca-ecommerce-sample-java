---
id: only-an-offered-shipping-method-is-taken-accept-1
story: only-an-offered-shipping-method-is-taken
stage: document
kind: acceptance
asked: 2026-10-20T09:44:50Z
digest: 78517f4d6dd9833473335bab73345f4d389bbdbf7ee29a3b69e461a30f5f66d6
---

# Accept only-an-offered-shipping-method-is-taken?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- an-unknown-shipping-method-is-refused: Given a guest on the delivery step; When the delivery form is sent with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country "Germany" and the shipping method "teleport"; Then the delivery step opens again with the error "This shipping method is not offered. Please choose another one." — `dev.domaincentric.sample.ecommerce.e2e.ChooseTheDeliveryAddressE2ETest#aShippingMethodTheDeliveryStepDoesNotOfferOpensItAgainWithTheRefusal`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-20T18:32:15Z
