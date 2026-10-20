---
id: the-back-office-header-is-the-operators-accept-1
story: the-back-office-header-is-the-operators
stage: document
kind: acceptance
asked: 2026-10-20T08:52:42Z
digest: 18d184cefccbd6eee7fc4bf226432448dc1bdf0191e04aca40a8cb1c4e76d7f9
---

# Accept the-back-office-header-is-the-operators?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-back-office-header-offers-the-operators-logout: Given the operator is logged in to the back office in a browser that is not logged in to the shop; When they open `/backoffice/events`; Then the page's header shows the shop's name, "Back office" and a "Logout" button; And it shows neither "Login" nor "Register" nor a "Cart" entry — `dev.domaincentric.sample.ecommerce.e2e.BackofficeHeaderE2ETest#eventLogHeaderShowsTheShopBackOfficeAndTheOperatorsLogout`
- the-back-office-header-shows-no-shopper: Given a browser logged in to the shop as "anna.schmidt@example.com" whose operator then logged in to the back office; When they open `/backoffice/events`; Then the page's header shows neither "Welcome, anna.schmidt@example.com" nor "My Account" nor a "Cart" entry — `dev.domaincentric.sample.ecommerce.backoffice.BackofficeHeaderIntegrationTest#eventLogHeaderShowsNothingOfTheShopperLoggedInToTheShop`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-20T18:32:15Z
