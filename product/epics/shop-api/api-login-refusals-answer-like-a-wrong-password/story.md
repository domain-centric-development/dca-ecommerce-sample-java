---
id: api-login-refusals-answer-like-a-wrong-password
epic: shop-api
status: approved
created: 2026-11-01T12:17:53Z
context: account
feature: signin
title: Every failed login through the API answers like a wrong password
depends_on: [product-refusals-name-the-field-in-the-shops-words, the-account-findings-are-closed, a-second-login-takes-over-no-account-as-guest]
---

# Every failed login through the API answers like a wrong password

## Story

As the shop owner I want every failed login through the API to answer alike — a missing or malformed address, a
missing password, a wrong password — so that a client learns nothing from the refusal but that the login failed,
as on the login page.

## Acceptance criteria

### Rule: Every failed login through the API answers alike

#### a-login-without-an-address-is-refused (happy path)
- Given an account "ada@example.com" with password "Secret123"
- When a client sends `POST /api/auth/login` with `{{"password":"Secret123"}}`
- Then the shop answers 401 with an `application/problem+json` body and the header `WWW-Authenticate: Bearer`
- And its title is "Authentication failed" and it carries no detail

#### a-login-with-a-malformed-address-is-refused
- Given an account "ada@example.com" with password "Secret123"
- When a client sends `POST /api/auth/login` with `{{"email":"ada@example","password":"Secret123"}}`
- Then the shop answers 401 with an `application/problem+json` body and the header `WWW-Authenticate: Bearer`
- And its title is "Authentication failed" and it carries no detail

#### a-login-without-a-password-is-refused
- Given an account "ada@example.com" with password "Secret123"
- When a client sends `POST /api/auth/login` with `{{"email":"ada@example.com"}}`
- Then the shop answers 401 with an `application/problem+json` body and the header `WWW-Authenticate: Bearer`
- And its title is "Authentication failed" and it carries no detail

## Changed expectations

- A login through the API without an address, with a malformed address or without a password is refused today
  with 400 and the framework's reasons per field; after, with 401 "Authentication failed" and no detail, like a
  wrong password.

## Assumptions

- answered: a failed login through the API answers like a wrong password, whatever was wrong with the input (the
  person, 2026-11-01, WP-170)
- answered: the request carries no annotation repeating a rule of the account's values (WP-170 decisions 1 and 2)

## Notes

- WP-170 (a rule is checked once, in the value object): `LoginRequest` checks email and password with request
  validation; without it, a login never builds a value that names a field — it either finds the account with
  that password or fails.
- `a-wrong-password-is-refused` holds as it is.
- Spec (`account/signin`): adds the rule "Every failed login through the API answers alike" with three scenarios.
