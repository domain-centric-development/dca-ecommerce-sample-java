---
id: every-cart-answer-shows-todays-price-accept-2
story: every-cart-answer-shows-todays-price
stage: document
kind: acceptance
asked: 2026-10-21T18:55:36Z
digest: 5f7ba8147b40161a2e8b7e109fa85067b51488b37581e711159f74c68fd64b97
---

# Accept every-cart-answer-shows-todays-price?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- adding-answers-at-todays-prices: Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR; And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API; When her client adds "Clean Architecture" with quantity 1; Then the shop answers 200 with the cart, whose "Domain-Driven Design" item has `price` 49.99 and whose `total` is 89.98; And no item of the answer carries a field `priceAtAddition` — `dev.domaincentric.sample.ecommerce.e2e.CartApiPriceE2ETest#addingToTheCartAnswersEveryItemAtTodaysPrice`
- removing-answers-at-todays-prices: Given Ada's cart holds "Domain-Driven Design" once, added at 54.99 EUR, and "Clean Architecture" once; And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API; When her client removes "Clean Architecture"; Then the shop answers 200 with the cart, whose "Domain-Driven Design" item has `price` 49.99 and whose `total` is 49.99 — `dev.domaincentric.sample.ecommerce.cart.RemoveItemFromCartPriceIntegrationTest#removingAProductAnswersTheRestOfTheCartAtTodaysPrice`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T19:36:18Z
