---
id: the-shop-sets-the-shipping-price-accept-1
story: the-shop-sets-the-shipping-price
stage: document
kind: acceptance
asked: 2026-10-15T10:32:39Z
digest: ea6564522dd8fc13d3a9d9ea193acf8f6dbeac4795f1f93323c18f724671ca06
---

# Accept the-shop-sets-the-shipping-price?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-shipping-price-sent-with-the-form-is-not-taken: Given a guest who started the checkout with "Domain-Driven Design" once and is on the delivery step; When the delivery form is sent with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country; Then the payment step opens and shows Subtotal "54.99 EUR", Shipping "19.99 EUR" and Total "74.98 EUR" — `dev.domaincentric.sample.ecommerce.e2e.ChooseTheDeliveryAddressE2ETest#theShippingPriceIsTheOneTheDeliveryStepOffersWhateverTheFormCarries`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-15T14:56:00Z
rationale: Accepted by the person (2026-10-15), recorded by the managing session.
