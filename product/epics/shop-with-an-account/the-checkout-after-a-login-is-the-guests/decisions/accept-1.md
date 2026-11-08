---
id: the-checkout-after-a-login-is-the-guests-accept-1
story: the-checkout-after-a-login-is-the-guests
stage: document
kind: acceptance
asked: 2026-11-03T15:54:44Z
digest: 44f4885902fa972c00cb4b7f0c0a246aace7e7c11e36e02e4fa41264eb0c993d
---

# Accept the-checkout-after-a-login-is-the-guests?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-login-returns-to-the-guests-checkout-not-an-older-one: Given an account with an open checkout from a cart it emptied since; And a guest who started a checkout of "Clean Architecture"; When the guest logs in to that account from the checkout; Then the checkout shows "Clean Architecture", not the account's older checkout — `dev.domaincentric.sample.ecommerce.e2e.LoginFromTheCheckoutE2ETest#aLoginFromTheCheckoutReturnsToTheGuestsCheckoutNotTheAccountsOlderOne`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.
