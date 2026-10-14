---
id: a-refused-api-login-is-a-problem-document
epic: shop-api
status: draft
context: account
feature: signin
title: A refused login through the API is a problem document
depends_on: []
---

# A refused login through the API is a problem document

## Story

As a partner application I want a refused login to answer in the same shape as every other refusal of the API, so
that one error handler reads all of them.

## Acceptance criteria

### Rule: Logging in through the API hands out a token for the account

#### a-wrong-password-is-refused (happy path)
- Given an account "ada@example.com" with password "Secret123"
- When a client sends `POST /api/auth/login` with `{"email":"ada@example.com","password":"Wrong1234"}`
- Then the shop answers 401 with an `application/problem+json` body
- And its title is "Login failed" and its detail "Invalid email or password"

## Changed expectations

- A refused login through the API answers 401 with `{"success":false,"token":null,"email":null,"errorMessage":"Invalid
  email or password"}` as `application/json` now; after, with a problem document.

## Assumptions

- open: the problem document's title and detail — "Login failed" / "Invalid email or password" (as written), or
  "Unauthorized" without detail, as a call without a token is answered?
- open: does the answer carry `WWW-Authenticate: Bearer`, as the challenge of a call without a token does?

## Notes

- From TODO #95 (a), auth API half, checked 2026-10-15: still true in both shops.
- Spec (`account/signin`): changes `a-wrong-password-is-refused` (the answer's shape); holds
  `logging-in-hands-out-a-token`.
