---
id: logging-in-from-the-checkout-returns-to-it-accept-1
story: logging-in-from-the-checkout-returns-to-it
stage: document
kind: acceptance
asked: 2026-10-21T07:55:57Z
digest: f84749678fc6a2e973cf406eddc6e4f89eb5de0627103d44a03fc00f1b8d5aa8
---

# Accept logging-in-from-the-checkout-returns-to-it?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- login-from-the-checkout-keeps-the-cart-not-the-checkout: Given the account "anna.schmidt@example.com", registered earlier, whose cart holds nothing; And a browser with no cookies in which a guest added "Domain-Driven Design" and pressed "Proceed to Checkout"; And the guest opened `/login?returnUrl=/checkout/buyer`; When they log in with "anna.schmidt@example.com" and "Secret123"; Then the buyer step `/checkout/buyer` opens; And its order summary shows "Domain-Driven Design x1" — `dev.domaincentric.sample.ecommerce.e2e.LoginFromTheCheckoutE2ETest#aGuestWhoLogsInFromTheBuyerStepReturnsToTheCheckoutWithTheOrder, dev.domaincentric.sample.ecommerce.cart.CartAtLoginIntegrationTest#loggingInFromTheBuyerStepReturnsThereWhenTheAccountsCartWasEmptied`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T11:05:34Z
