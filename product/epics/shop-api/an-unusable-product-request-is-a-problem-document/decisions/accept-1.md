---
id: an-unusable-product-request-is-a-problem-document-accept-1
story: an-unusable-product-request-is-a-problem-document
stage: document
kind: acceptance
asked: 2026-10-20T13:18:54Z
digest: c5100b9fc3a143ffe1c455e86cf6bf39d56e7da3f57504c081bde979c932f840
---

# Accept an-unusable-product-request-is-a-problem-document?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-blank-name-is-refused: Given a client holding a staff token; When it creates a product with SKU "BLANK-001", name "   ", price 9.99, category "Books" and stock 1; Then the shop answers 400 with an `application/problem+json` body; And its title is "Unacceptable value" and its `errors` names `name` with the reason; And the catalogue still holds 21 products — `dev.domaincentric.sample.ecommerce.e2e.RefusedProductValueApiE2ETest#aBlankNameIsAnUnacceptableValueNamingTheNameAndNothingIsCreated`
- a-price-of-zero-is-refused: Given a client holding a staff token; When it creates "Free Mug" with SKU "FREE-001", price 0, category "Desk & Office" and stock 5; Then the shop answers 400 with an `application/problem+json` body; And its title is "Unacceptable value" and its `errors` names `price` with the reason; And the catalogue still holds 21 products — `dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockRefusalIntegrationTest#aPriceOfZeroIsAnUnacceptableValueNamingThePriceAndNothingIsCreated`
- a-negative-initial-stock-is-refused: Given a client holding a staff token; When it creates "Owed Pin" with SKU "PIN-003", price 12.99, category "Stickers & Pins" and stock -1; Then the shop answers 400 with an `application/problem+json` body; And its title is "Unacceptable value" and its `errors` names `stock` with the reason; And the catalogue still holds 21 products — `dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockRefusalIntegrationTest#aNegativeInitialStockIsAnUnacceptableValueNamingTheStockAndNothingIsCreated`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-20T18:32:15Z
