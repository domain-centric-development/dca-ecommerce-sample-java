---
id: the-shops-forms-refuse-posts-that-did
epic: fits-its-surroundings
status: adopted
context: account
title: The shop's forms refuse posts that did not come from its pages
depends_on: [register-an-account, log-in-and-log-out]
---

# The shop's forms refuse posts that did not come from its pages

## Story

As a customer I want the shop to act on a form only when I sent it from the shop's own page, so that a foreign
page open in my browser cannot log me in, register me or log me out behind my back.

## Acceptance criteria

<!-- happy-path: a-login-post-without-the-form-token-is-refused -->

### Rule: A form post without the token of the shop's page is refused with 403

#### a-login-post-without-the-form-token-is-refused
- Given an account "ada@example.com" with password "Secret123"
- When a browser posts email "ada@example.com" and password "Secret123" to `/login` without the form token the login page carries
- Then the shop answers 403
- And the visitor is not logged in: the header still shows "Login" and "Register"

#### a-registration-post-without-the-form-token-is-refused
- Given no account exists for "grace@example.com"
- When a browser posts a complete, valid registration for "grace@example.com" to `/register` without the form token
- Then the shop answers 403
- And logging in as "grace@example.com" afterwards fails with "Invalid email or password"

#### a-logout-post-without-the-form-token-is-refused
- Given a customer logged in as "ada@example.com"
- When a post to `/logout` arrives from her browser without the form token
- Then the shop answers 403
- And the header still shows "Welcome, ada@example.com"

## Out of scope

- A form sent from the shop's own page, with its token: it goes through as the owning story describes.
- The add-to-cart form without its token — the cart epic.
- API and assistant calls: they need no form token — `the-api-trusts-only-the-token`.

## Notes

- Replay story: `FIT-05` (fits-its-surroundings).
- Inventory: cross-cutting X1 (X2 is the normal flow of every form story).
