---
id: a-second-login-takes-over-no-account-as-guest-accept-1
story: a-second-login-takes-over-no-account-as-guest
stage: document
kind: acceptance
asked: 2026-11-03T15:32:44Z
digest: 59e8e47257865c85cd964780f331e0e500f252de9e6cf562a45e0eb786e60024
---

# Accept a-second-login-takes-over-no-account-as-guest?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-login-page-asks-a-logged-in-customer-to-log-out: Given the customer "anna.schmidt@example.com" is logged in; When they open `/login`; Then the page shows "You are logged in as anna.schmidt@example.com. Please log out first."; And it shows a "Logout" button and no login form — `dev.domaincentric.sample.ecommerce.e2e.LoginWhileLoggedInE2ETest#theLoginPageAsksALoggedInCustomerToLogOutFirst`
- a-login-sent-while-logged-in-changes-nothing: Given the customer "anna.schmidt@example.com" is logged in and their cart holds "Clean Architecture" once; And the account "ben.weber@example.com" with password "Secret123" and an empty cart; When the browser sends the login form for "ben.weber@example.com" with "Secret123"; Then the page shows "You are logged in as anna.schmidt@example.com. Please log out first."; And the header still shows "Welcome, anna.schmidt@example.com"; And Anna's cart still holds "Clean Architecture" with quantity 1, and Ben's cart is empty — `dev.domaincentric.sample.ecommerce.account.LoginWhileLoggedInIntegrationTest#aLoginSentWhileLoggedInShowsTheNoticeAndMovesNoCart`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Blömer
at: 2026-11-03T16:15:38Z
