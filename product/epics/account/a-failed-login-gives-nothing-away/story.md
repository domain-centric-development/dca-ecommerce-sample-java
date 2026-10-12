---
id: a-failed-login-gives-nothing-away
epic: account
status: adopted
context: account
feature: signin
title: A failed login gives nothing away
depends_on: [log-in-and-log-out]
---

# A failed login gives nothing away

## Story

As a customer I want a failed login to tell me that it failed, but not whether the address or the password was
wrong, so that nobody can find out through the login which addresses have an account.

## Acceptance criteria

<!-- happy-path: wrong-password-is-refused -->

### Rule: Every failed login shows the same message and keeps the typed email

#### wrong-password-is-refused
- Given the account "anna.schmidt@example.com" with password "Secret123" and a browser with no cookies on `/login`
- When the visitor logs in with "anna.schmidt@example.com" and "Secret999"
- Then the login page shows "Invalid email or password"
- And the Email field still holds "anna.schmidt@example.com" and the Password field is empty
- And the header still shows "Login" and "Register"

#### unknown-address-is-refused
- Given no account for "nobody@example.com" and a browser with no cookies on `/login`
- When the visitor logs in with "nobody@example.com" and "Secret123"
- Then the login page shows "Invalid email or password"

#### malformed-address-is-refused
- Given a browser with no cookies on `/login`
- When the visitor logs in with "anna.schmidt@example" and "Secret123"
- Then the login page shows "Invalid email or password"

## Out of scope

- The login refusal through the API — `shop-api`.
- A suspended or closed account: nothing in the shop can suspend or close an account, so the refusal cannot be
  reached from outside.

## Notes

- Replay story: `ACC-05` (account).
- Inventory: account A13.
