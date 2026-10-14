---
id: a-malformed-product-id-is-a-bad-request-accept-1
story: a-malformed-product-id-is-a-bad-request
stage: document
kind: acceptance
asked: 2026-10-14T21:34:43Z
digest: 76623e3fb690803ba2b892291b6a38e25921603481db052b6145ba32ab40be83
---

# Accept a-malformed-product-id-is-a-bad-request?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-malformed-product-id-is-a-bad-request: Given Ada's open, empty cart; When her client sends `POST /api/carts/<cartId>/items` with `{"productId":"no-such-product","quantity":1}`; Then the shop answers 400 with an `application/problem+json` body; And its title is "Unacceptable value" and it carries no detail; And her cart is still empty — `dev.domaincentric.sample.ecommerce.e2e.ShoppingCartApiE2ETest#addingAProductIdThatIsNoUuidIsABadRequestAndLeavesTheCartEmpty`
- removing-a-malformed-product-id-is-a-bad-request: Given Ada's cart holds "Domain-Driven Design" × 1; When her client sends `DELETE /api/carts/<cartId>/items/no-such-product`; Then the shop answers 400 with an `application/problem+json` body; And its title is "Unacceptable value" and it carries no detail; And her cart still holds "Domain-Driven Design" × 1 — `dev.domaincentric.sample.ecommerce.cart.ShoppingCartApiIntegrationTest#removingAProductIdThatIsNoUuidIsABadRequestAndLeavesTheCartAsItWas`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-14T21:50:57Z
rationale: Accepted on the person's behalf by the managing session (authorized 2026-10-14 21:35 for the remaining WP-108 queue).
