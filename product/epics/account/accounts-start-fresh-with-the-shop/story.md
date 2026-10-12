---
id: accounts-start-fresh-with-the-shop
epic: account
status: adopted
context: account
feature: registration
title: Accounts start fresh with the shop
depends_on: [my-account-overview]
---

# Accounts start fresh with the shop

## Story

As a customer whose account was lost because the shop was restarted I want the shop to treat me as a visitor,
without an error, so that I can simply register again and carry on shopping.

## Acceptance criteria

<!-- happy-path: restart-turns-the-customer-into-a-visitor -->

### Rule: After a restart the shop knows no account, and a logged-in browser quietly becomes a visitor

#### restart-turns-the-customer-into-a-visitor
- Given the customer "anna.schmidt@example.com" is logged in and the shop has been restarted since
- When they open the catalogue page `/products` in the same browser
- Then the page shows the catalogue without an error message
- And the header shows "Login" and "Register" instead of "Welcome, anna.schmidt@example.com"

#### account-pages-send-to-the-login-after-a-restart
- Given the customer "anna.schmidt@example.com" was logged in and the shop has been restarted since
- When they open `/account` in the same browser
- Then they are sent to `/login?returnUrl=%2Faccount`

#### old-credentials-are-refused-after-a-restart
- Given the account "anna.schmidt@example.com" with password "Secret123" existed before the shop was restarted
- When the visitor logs in with "anna.schmidt@example.com" and "Secret123"
- Then the login page shows "Invalid email or password"

## Out of scope

- Registering again after the restart works like any first registration — `register-an-account`.

## Notes

- Replay story: `ACC-12` (account).
- The shop keeps nothing across a restart (product description, "How it works").
- Inventory: account A20 (as far as reachable), A21.
