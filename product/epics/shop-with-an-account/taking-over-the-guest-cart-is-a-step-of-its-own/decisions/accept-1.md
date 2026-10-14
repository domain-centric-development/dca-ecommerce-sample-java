---
id: taking-over-the-guest-cart-is-a-step-of-its-own-accept-1
story: taking-over-the-guest-cart-is-a-step-of-its-own
stage: document
kind: acceptance
asked: 2026-10-14T23:46:17Z
digest: d7831283b48dcd6b2fd3e2f0ef8588fb338bb0929aa45738209a1efd7bd7b14a
---

# Accept taking-over-the-guest-cart-is-a-step-of-its-own?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-page-after-the-login-moves-no-cart: Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier, whose cart holds nothing; And a browser with no cookies that runs no script, in which a guest added `"Ubiquitous Language" Mug` to the cart; When the guest logs in on `/login` with "anna.schmidt@example.com" and "Secret123"; Then a page opens that says "Bringing your cart along." and offers a "Continue" button; And the account's cart still holds nothing — `dev.domaincentric.sample.ecommerce.cart.CartAtLoginIntegrationTest#openingThePageAfterTheLoginLeavesTheAccountsCartAsItWas`
- continue-takes-the-guest-cart-over: Given the account "anna.schmidt@example.com", whose cart holds nothing, and a guest in a browser that runs no script; When they press "Continue"; Then the cart page `/cart` opens and shows "Welcome back!"; And the cart shows one line `"Ubiquitous Language" Mug` with quantity 1 — `dev.domaincentric.sample.ecommerce.e2e.CartTakeOverWithoutScriptE2ETest#continueTakesTheGuestCartIntoTheAccount`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-14T23:49:49Z
rationale: Accepted on the person's behalf by the managing session (authorized 2026-10-14 21:35 for the remaining WP-108 queue). The guest id taken from the form and the open returnUrl are TODO #90, drafted as only-the-browsers-own-guest-cart-is-taken-over and login-leads-only-to-shop-pages.
