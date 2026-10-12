---
id: one-account-per-address-and-per-browser
epic: account
status: adopted
context: account
feature: registration
title: One account per address and per browser
depends_on: [register-an-account]
---

# One account per address and per browser

## Story

As a customer I want no one else to be able to register with my address, and the shop to stop me from
creating a second account by mistake, so that my address always leads to my own account.

## Acceptance criteria

<!-- happy-path: taken-address-is-refused -->

### Rule: An address can belong to one account only, whatever its capitals and surrounding spaces

#### taken-address-is-refused
- Given the account "anna.schmidt@example.com", registered earlier, and another visitor with no cookies on `/register`
- When they submit a valid registration with Email "anna.schmidt@example.com"
- Then the registration page shows "Email is already registered: anna.schmidt@example.com"
- And the visitor is not logged in

#### taken-address-in-other-capitals-is-refused
- Given the account "anna.schmidt@example.com", registered earlier, and another visitor with no cookies on `/register`
- When they submit a valid registration with Email " Anna.Schmidt@Example.COM "
- Then the registration page shows "Email is already registered: anna.schmidt@example.com"

### Rule: A browser that already has an account cannot register a second one

#### second-registration-from-the-same-browser-is-refused
- Given a visitor who just registered "anna.schmidt@example.com" and is logged in
- When they open `/register` and submit a valid registration with Email "anna.second@example.com"
- Then the registration page shows "User already has an account"
- And the header still shows "Welcome, anna.schmidt@example.com"

## Out of scope

- A taken address in the profile — `the-profile-refuses-an-unusable-change`.
- The same refusals through the API — `shop-api`.

## Notes

- Replay story: `ACC-04` (account).
- Inventory: account A8, A9.
