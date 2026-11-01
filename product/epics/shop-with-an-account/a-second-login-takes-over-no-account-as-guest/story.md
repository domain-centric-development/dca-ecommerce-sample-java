---
id: a-second-login-takes-over-no-account-as-guest
epic: shop-with-an-account
status: draft
created: 2026-10-23T21:25:20Z
context: account
feature: signin
title: A logged-in browser logs out before it logs in again
closes: [only-the-browsers-own-guest-cart-is-taken-over#1]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# A logged-in browser logs out before it logs in again

## Story

As a customer I want the shop to tell me to log out before I log in to another account, so that logging in never
moves one account's cart into another and I always know which account I am in.

## Acceptance criteria

### Rule: A logged-in browser cannot log in again; it logs out first

#### the-login-page-asks-a-logged-in-customer-to-log-out (happy path)
- Given the customer "anna.schmidt@example.com" is logged in
- When they open `/login`
- Then the page shows "You are logged in as anna.schmidt@example.com. Please log out first."
- And it shows a "Logout" button and no login form

#### a-login-sent-while-logged-in-changes-nothing
- Given the customer "anna.schmidt@example.com" is logged in and their cart holds "Clean Architecture" once
- And the account "ben.weber@example.com" with password "Secret123" and an empty cart
- When the browser sends the login form for "ben.weber@example.com" with "Secret123"
- Then the page shows "You are logged in as anna.schmidt@example.com. Please log out first."
- And the header still shows "Welcome, anna.schmidt@example.com"
- And Anna's cart still holds "Clean Architecture" with quantity 1, and Ben's cart is empty

## Changed expectations

- A logged-in customer who opens the login page sees the login form today, and a login sent while logged in
  switches the browser to the other account; after, the page asks them to log out first and nothing changes.

## Assumptions

- answered: a logged-in browser cannot log in again; it has to log out first (the person, 2026-11-01)
- answered: the login page then shows the account it is logged in as, the request to log out first and a logout
  button; a login sent anyway changes nothing and shows the same page (the person, 2026-11-01)

## Notes

- From the findings triage of 2026-10-23 (WP-151): the login records the logged-in account as the guest whose cart
  may follow — same finding in both shops. Refusing the second login removes the case instead of handling it.
- Registration already refuses a browser that has an account ("A browser that already has an account cannot
  register a second one"); this is the login's twin.
- The API is not touched: it keeps no session, a client logs in by sending credentials.
