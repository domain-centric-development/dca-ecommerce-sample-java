---
id: an-unusable-cart-request-is-a-problem-document-accept-1
story: an-unusable-cart-request-is-a-problem-document
stage: document
kind: acceptance
asked: 2026-10-21T06:57:40Z
digest: 436f38d051f443ff95776509c3a83ad782af0b4fe45f385827b9532d0c019430
---

# Accept an-unusable-cart-request-is-a-problem-document?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-quantity-of-zero-is-refused: Given Ada's open, empty cart; When her client adds "Domain-Driven Design" with quantity 0; Then the shop answers 400 with an `application/problem+json` body; And its title is "Unacceptable value" and its `errors` names `quantity` with the reason; And her cart is still empty — `dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#addingAQuantityOfZeroIsAnUnacceptableValueNamingTheQuantityAndLeavesTheCartEmpty`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T11:05:34Z
