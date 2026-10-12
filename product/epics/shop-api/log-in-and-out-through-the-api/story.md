---
id: log-in-and-out-through-the-api
epic: shop-api
status: adopted
context: account
feature: signin
title: Log in and out through the API
depends_on: [register-through-the-api]
---

# Log in and out through the API

## Story

As a partner application I want to log in through the API and receive a token, and to log out again, so that
I can act for a customer without the shop's pages.

## Acceptance criteria

<!-- happy-path: logging-in-hands-out-a-token -->

### Rule: Logging in through the API hands out a token for the account

#### logging-in-hands-out-a-token
- Given an account "ada@example.com" with password "Secret123"
- When a client sends `POST /api/auth/login` with `{"email":"ada@example.com","password":"Secret123"}`
- Then the shop answers 200
- And the body has `success` true, a non-empty `token` and `email` "ada@example.com"

#### a-wrong-password-is-refused
- Given an account "ada@example.com" with password "Secret123"
- When a client sends `POST /api/auth/login` with `{"email":"ada@example.com","password":"Wrong1234"}`
- Then the shop answers 401

### Rule: Logging out through the API is the client discarding its token

#### logging-out-answers-no-content
- Given a client holding the token of the account "ada@example.com"
- When it sends `POST /api/auth/logout`
- Then the shop answers 204 with an empty body

## Out of scope

- The body of a 400 caused by a missing or blank field, and the body of the 401 on a failed login: their shape is
  not replayed.
- Refusing a suspended or closed account: nothing in the shop suspends or closes an account, so it is not
  reachable from outside.
- Using the token on the product and cart routes — `staff-create-a-product-through-the-api`, `open-and-fill-a-cart-through-the-api`; that the API never reads or sets cookies —
  `the-api-trusts-only-the-token`.
- Registering and logging in on the pages — `register-an-account`, `log-in-and-log-out`.

## Notes

- Replay story: `API-01` (shop-api), its sign-in part.
- Inventory: account A30, A31, A32 (reachable refusals), A33.
