---
id: created-product-answers-with-its-rounded-price-accept-1
story: created-product-answers-with-its-rounded-price
stage: document
kind: acceptance
asked: 2026-10-13T18:39:09Z
digest: 029a4439888c74351b3ba5f1690572b4d2ca707bde07d3d01f01c38207579f68
---

# Accept created-product-answers-with-its-rounded-price?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-answer-carries-the-rounded-price: Given a client holding a staff token; When it creates "Rounding Mug" with SKU "ROUND-001", price 14.995, category "Desk & Office" and stock 5; Then the shop answers 201 with `price` 15.00 — `dev.domaincentric.sample.ecommerce.e2e.ProductCreationApiE2ETest#theAnswerCarriesThePriceRoundedHalfUpToCents`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-13T18:41:22Z
rationale: accepted from the factory cockpit.
