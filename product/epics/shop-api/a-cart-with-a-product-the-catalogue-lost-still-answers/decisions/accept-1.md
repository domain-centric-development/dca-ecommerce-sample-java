---
id: a-cart-with-a-product-the-catalogue-lost-still-answers-accept-1
story: a-cart-with-a-product-the-catalogue-lost-still-answers
stage: document
kind: acceptance
asked: 2026-11-03T12:52:56Z
digest: d9fd84587a93f4da4e6cf21e4cbd6f07edaac407ec8b2db054a26081ab688ab4
---

# Accept a-cart-with-a-product-the-catalogue-lost-still-answers?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-cart-answers-with-a-product-the-catalogue-lost: Given a cart holding "Clean Architecture" once and "Domain-Driven Design" once; And the catalogue no longer knows "Domain-Driven Design"; When "Clean Architecture" is added once more through the API; Then the answer is 200 and the cart holds "Clean Architecture" with quantity 2; And the line of "Domain-Driven Design" holds `quantity` 1, `available` false and no `price`; And `total` is the price of the two units of "Clean Architecture" alone — `dev.domaincentric.sample.ecommerce.e2e.CartWithALostProductApiE2ETest#addingToACartWithAProductTheCatalogueLostAnswersWithTheLineUnavailableAndUnpriced`
- the-staff-list-answers-with-a-product-the-catalogue-lost: Given the cart above; When a member of staff reads the list of every cart; Then the answer is 200 and lists the cart, its line of "Domain-Driven Design" with `available` false and no `price` — `dev.domaincentric.sample.ecommerce.cart.CartWithALostProductIntegrationTest#theStaffListShowsALineWhoseProductTheCatalogueLostAsUnavailableWithoutPrice`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Blömer
at: 2026-11-03T13:07:08Z
