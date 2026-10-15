---
id: an-unknown-product-page-answers-not-found-accept-1
story: an-unknown-product-page-answers-not-found
stage: document
kind: acceptance
asked: 2026-10-15T08:46:32Z
digest: 0a01d290d59a1eec7a3cae9442d7e7056d74fe8224abba77a3364688fe88c3ce
---

# Accept an-unknown-product-page-answers-not-found?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- an-unknown-product-answers-404: Given the catalogue has no product with the id "00000000-0000-4000-8000-000000000001"; When a visitor opens `/products/00000000-0000-4000-8000-000000000001`; Then the shop answers with status 404; And the page shows "404", "Page Not Found" and "The page you're looking for doesn't exist or has been removed. — `dev.domaincentric.sample.ecommerce.e2e.ProductNotFoundE2ETest#unknownProductAddressAnswersNotFound`
- a-malformed-product-address-answers-404: Given a visitor with no cookies; When they open `/products/no-such-product`; Then the shop answers with status 404; And the page shows "Page Not Found" — `dev.domaincentric.sample.ecommerce.product.ProductPageNotFoundStatusIntegrationTest#malformedProductAddressAnswersNotFound`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-15T14:56:00Z
rationale: Accepted by the person (2026-10-15), recorded by the managing session.
