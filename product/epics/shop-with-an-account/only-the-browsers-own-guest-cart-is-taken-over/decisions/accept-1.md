---
id: only-the-browsers-own-guest-cart-is-taken-over-accept-1
story: only-the-browsers-own-guest-cart-is-taken-over
stage: document
kind: acceptance
asked: 2026-10-21T13:12:08Z
digest: e65fb7361d5734d31ee064588182f873d5da3145c4b13e48fd5a669117b5d4e7
---

# Accept only-the-browsers-own-guest-cart-is-taken-over?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- another-guests-cart-is-not-taken-over: Given the account "anna.schmidt@example.com", whose cart holds nothing, logged in in a browser that had an empty guest cart; And in another browser a guest added "Clean Architecture" to the cart; When Anna's browser sends the cart take-over for the other browser's guest cart; Then the cart page `/cart` opens and Anna's cart holds nothing; And the other guest's cart still holds "Clean Architecture" with quantity 1 — `dev.domaincentric.sample.ecommerce.e2e.GuestCartOfAnotherBrowserE2ETest#theTakeOverOfAnotherBrowsersGuestCartMovesNothing`
- another-guests-cart-is-not-merged: Given the account "anna.schmidt@example.com", whose cart holds "Clean Architecture" once, logged in in a browser that had an empty guest cart; And in another browser a guest added `"Ubiquitous Language" Mug` to the cart; When Anna's browser sends the merge choice "Merge Both Carts" for the other browser's guest cart; Then the cart page `/cart` opens and Anna's cart holds only "Clean Architecture" with quantity 1; And the other guest's cart still holds `"Ubiquitous Language" Mug` with quantity 1 — `dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#theMergeOfAnotherBrowsersGuestCartMovesNothing`
- an-outside-return-page-after-the-merge-opens-the-cart: Given the two carts, and the guest opened `/login?returnUrl=https://elsewhere.example/`, logged in and is on the merge page; When they press "Continue" with "Merge Both Carts" chosen; Then the cart page `/cart` opens and shows "Carts merged successfully!" — `dev.domaincentric.sample.ecommerce.cart.CartMergeChoiceIntegrationTest#theMergeLeadsToTheCartInsteadOfAReturnPageOfAnotherSite`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T14:58:20Z
