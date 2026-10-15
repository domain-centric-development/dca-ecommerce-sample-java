---
id: a-refused-api-login-is-a-problem-document
epic: shop-api
status: approved
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
- Then the shop answers 401 with an `application/problem+json` body and the header `WWW-Authenticate: Bearer`
- And its title is "Authentication failed" and it carries no detail

## Changed expectations

- A refused login through the API answers 401 with `{"success":false,"token":null,"email":null,"errorMessage":"Invalid
  email or password"}` as `application/json` now; after, with a problem document titled "Authentication failed",
  without detail, and the header `WWW-Authenticate: Bearer`.

## Assumptions

- answered: a refused API login answers 401 with a problem document, title "Authentication failed", no detail, and
  `WWW-Authenticate: Bearer` (planning/questions-2026-10-15.md Q4.6)

## Notes

- From TODO #95 (a), auth API half, checked 2026-10-15: still true in both shops.
- Spec (`account/signin`): changes `a-wrong-password-is-refused` (the answer's shape); holds
  `logging-in-hands-out-a-token`.
