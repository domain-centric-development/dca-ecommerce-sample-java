---
id: remove-a-line-from-the-cart-accept-1
story: remove-a-line-from-the-cart
stage: document
kind: acceptance
asked: 2026-10-14T17:20:27Z
digest: a5ebd7a3cc806070b687d3bdf6dadd52493cb8ae6eebb3ac48174344e0e27474
---

# Accept remove-a-line-from-the-cart?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- remove-takes-the-line-out: Given a guest who added "Domain-Driven Design", "Clean Architecture" and "Team Topologies" once each, in that order; When they press "Remove" on the line "Clean Architecture" on the cart page; Then the cart page shows two lines, "Domain-Driven Design" first and "Team Topologies" second; And the header's cart entry shows the badge "2" — `dev.domaincentric.sample.ecommerce.e2e.RemoveCartLineE2ETest#removingALineLeavesTheOtherLinesInTheirOrder`
- remove-takes-the-whole-quantity: Given a guest whose cart holds "Domain-Driven Design" with quantity 3 and "Clean Architecture" with quantity 1; When they press "Remove" on the line "Domain-Driven Design"; Then the cart page shows one line, "Clean Architecture" with quantity 1; And it shows "Total Items: 1" and "Total Quantity: 1" — `dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#removingALineTakesItsWholeQuantityOut`
- removing-the-last-line-empties-the-cart: Given a guest whose cart holds only "Domain-Driven Design" with quantity 1; When they press "Remove" on that line; Then the cart page shows "Your cart is empty" — `dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#removingTheLastLineLeavesAnEmptyCart`
- removing-a-line-already-gone-changes-nothing: Given a guest who opened the cart page with "Domain-Driven Design" and "Clean Architecture" in two tabs and; When they press "Remove" on "Clean Architecture" in the second tab; Then the cart page opens and shows the one line "Domain-Driven Design" — `dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#removingALineAnotherTabRemovedAlreadyChangesNothing`
- a-remove-with-a-malformed-line-id-changes-nothing: Given a guest whose cart holds "Domain-Driven Design" with quantity 1; When a "Remove" naming the line "no-such-line" is sent to the shop; Then the cart page opens and still shows "Domain-Driven Design" with quantity 1 — `dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#aRemoveNamingNoLineOfTheCartChangesNothing`
- a-remove-for-another-visitors-line-changes-nothing: Given a guest whose cart holds `"Ubiquitous Language" Mug` and a second guest in another browser whose cart holds; When the second guest sends a "Remove" naming the first guest's `"Ubiquitous Language" Mug` line; Then the first guest's cart still shows `"Ubiquitous Language" Mug` with quantity 1; And the second guest's cart page still shows "Team Topologies" — `dev.domaincentric.sample.ecommerce.cart.RemoveCartItemIntegrationTest#aRemoveForAnotherVisitorsLineChangesNothing`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-14T17:25:17Z
