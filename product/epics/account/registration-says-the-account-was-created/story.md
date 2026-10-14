---
id: registration-says-the-account-was-created
epic: account
status: approved
context: account
feature: registration
title: The page a registration leads to says the account was created
depends_on: []
---

# The page a registration leads to says the account was created

## Story

As a visitor who has just registered I want the page my registration leads to to tell me that the account was
created, so that I know it worked.

## Acceptance criteria

### Rule: The page a successful registration leads to says once "Account created successfully! Welcome!"

#### registration-says-the-account-was-created (happy path)
- Given a visitor with no cookies on `/register`
- When they submit First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17, Email "anna.schmidt@example.com",
  Password "Secret123" and Confirm Password "Secret123"
- Then the home page `/` opens and shows "Account created successfully! Welcome!"

#### the-page-returned-to-says-the-account-was-created
- Given a visitor with no cookies who opened `/register?returnUrl=/products`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then the catalogue page `/products` opens and shows "Account created successfully! Welcome!"

## Out of scope

- Registering through the API, which answers with its token and shows no page.

## Changed expectations

- Registering leads to the home page, or to the page the visitor came from, without a word now, and with
  "Account created successfully! Welcome!" after.

## Notes

- Decided by the user 2026-10-14 (WP-108 side effect S2): a customer who registers sees that it worked. The sentence
  travels with the redirect as a notice addressed to the page the registration leads to (`product/tech.md`,
  Runtime), so it is shown there once and on no later page — that already holds today, where the sentence is shown
  nowhere, and stays a guarantee rather than a criterion.
