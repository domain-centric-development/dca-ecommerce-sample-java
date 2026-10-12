---
id: the-api-trusts-only-the-token
epic: shop-api
status: adopted
context: account
feature: signin
title: The API trusts only the token
depends_on: [staff-create-a-product-through-the-api, open-and-fill-a-cart-through-the-api, log-in-and-log-out]
---

# The API trusts only the token

## Story

As the shop owner I want the API to be reached only with a token in the request header, never with the browser
cookies of the shop's pages, so that a foreign page in a customer's browser cannot act through the API on the
customer's behalf.

## Acceptance criteria

<!-- happy-path: a-page-session-does-not-open-the-api -->

### Rule: A browser session of the pages does not authenticate an API call

#### a-page-session-does-not-open-the-api
- Given a customer logged in on the shop's pages, holding the pages' session cookies
- When the same browser sends `GET /api/carts` with those cookies and no `Authorization` header
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`

### Rule: The API hands out no cookies

#### api-answers-set-no-cookie
- Given a client without any cookies
- When it registers "ada@example.com" through `POST /api/auth/register`
- Then the answer is 201 and carries no `Set-Cookie` header

### Rule: A token the shop did not sign grants nothing

#### an-altered-token-is-a-stranger
- Given the token of the customer "ada@example.com" with one character of its signature changed
- When a client sends `POST /api/products` with a valid product and that token as `Authorization: Bearer`
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`

## Out of scope

- That API and assistant calls need no form token: they carry no cookies, so there is nothing to forge; every API
  story already calls without one.
- Cookies on the pages — `a-site-of-the-shops-own-can-embed-it`, and the account epics.

## Notes

- Replay story: `API-09` (shop-api).
- Inventory: account A34, A37 (Bearer part); cart C-60 (cookies); cross-cutting X3.
