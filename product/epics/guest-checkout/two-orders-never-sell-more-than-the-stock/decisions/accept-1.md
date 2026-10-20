---
id: two-orders-never-sell-more-than-the-stock-accept-1
story: two-orders-never-sell-more-than-the-stock
stage: document
kind: acceptance
asked: 2026-10-20T10:50:37Z
digest: 03820833b21543f4909b0206e9aece77f951005b7f4644e39904c41634b13978
---

# Accept two-orders-never-sell-more-than-the-stock?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-order-the-stock-no-longer-covers-is-refused: Given the seeded "Wooden Hexagon Desk Model" with a stock of 8; And two guests, each at the review with "Wooden Hexagon Desk Model" five times; When both press "Place Order" at the same moment; Then exactly one of them sees the confirmation; And the other is back at the review `/checkout/review` with "Not enough stock left for 'Wooden Hexagon Desk Model'. Please adjust your order."; And shortly afterwards the product page of "Wooden Hexagon Desk Model" reads "3 units available" — `dev.domaincentric.sample.ecommerce.e2e.TwoOrdersAtOnceE2ETest#ofTwoOrdersPlacedAtOnceOnlyTheOneTheStockCoversIsPlaced`
- place-order-is-refused-when-stock-ran-short: Given a guest at the review with "Wooden Hexagon Desk Model" five times; And another visitor has since bought it five times, leaving 3; When the guest presses "Place Order"; Then the review step stays open at `/checkout/review` and shows "Not enough stock left for 'Wooden Hexagon Desk Model'. Please adjust your order."; And no confirmation is shown; And the product page still reads "3 units available" and the guest's cart still holds the product five times — `dev.domaincentric.sample.ecommerce.checkout.OrderCheckedAgainstStockIntegrationTest#placingTheOrderAfterAnotherVisitorBoughtTheStockAwayIsRefused`
- place-order-is-refused-when-sold-out: Given a guest at the review with "Wooden Hexagon Desk Model" twice; And another visitor has since bought all 8; When the guest presses "Place Order"; Then the review step stays open at `/checkout/review` and shows "Not enough stock left for 'Wooden Hexagon Desk Model'. Please adjust your order."; And no confirmation is shown — `dev.domaincentric.sample.ecommerce.checkout.OrderCheckedAgainstStockIntegrationTest#placingTheOrderAfterAnotherVisitorBoughtEverythingIsRefused`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-20T18:32:15Z
