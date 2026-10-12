---
id: register-through-the-api
epic: shop-api
status: adopted
context: account
feature: registration
title: Register through the API
depends_on: [register-an-account, log-in-and-log-out]
---

# Register through the API

## Story

As a partner application I want to register an account through the API and receive a token, so that
I can act for a new customer without the shop's pages.

## Acceptance criteria

<!-- happy-path: registering-hands-out-a-token -->

### Rule: Registering through the API creates an account and hands out its token

#### registering-hands-out-a-token
- Given no account exists for "ada@example.com"
- When a client sends `POST /api/auth/register` with `{"email":"ada@example.com","password":"Secret123","firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1815-12-10"}`
- Then the shop answers 201
- And the body has `success` true, a non-empty `token` and `email` "ada@example.com"

### Rule: Registration refusals are problem documents

#### an-address-already-registered-is-a-conflict
- Given an account "ada@example.com"
- When a client sends `POST /api/auth/register` with email "ada@example.com", password "Secret123", first name "Grace", last name "Hopper" and date of birth "1906-12-09"
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Email already registered" and its detail "Email is already registered: ada@example.com"

#### a-weak-password-is-refused
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with password "secret123"
- Then the shop answers 422 with an `application/problem+json` body
- And its title is "Password too weak" and its detail "Password must contain at least one uppercase letter"

#### a-malformed-address-is-refused
- Given no account exists for "grace@example"
- When a client registers "grace@example" through the API with password "Secret123"
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its detail "Invalid email format: grace@example"

#### a-caller-with-an-account-cannot-register-another
- Given a client holding the token of the account "ada@example.com"
- When it sends `POST /api/auth/register` for "grace@example.com" with `Authorization: Bearer <that token>`
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Account already exists" and its detail "User already has an account"

## Out of scope

- The body of a 400 caused by a missing or blank field, and the body of the 401 on a failed login: their shape is
  not replayed.
- Refusing a suspended or closed account: nothing in the shop suspends or closes an account, so it is not
  reachable from outside.
- Using the token on the product and cart routes — `staff-create-a-product-through-the-api`, `open-and-fill-a-cart-through-the-api`; that the API never reads or sets cookies —
  `the-api-trusts-only-the-token`.
- Registering and logging in on the pages — `register-an-account`, `log-in-and-log-out`.

## Notes

- Replay story: `API-01` (shop-api), its registration part.
- Inventory: account A30, A31, A32 (reachable refusals), A33.
