---
id: operator-login-to-the-back-office
epic: run-the-shop
status: adopted
context: backoffice
feature: access
title: Operator login to the back office
depends_on: []
---

# Operator login to the back office

## Story

As an operator I want to log in to a back office of my own, separate from the shop's customer login, so that I
can look at what the running shop did without being a customer.

## Acceptance criteria

<!-- happy-path: login-opens-the-event-log -->

### Rule: The back office has its own login page

#### backoffice-login-page
- Given a browser with no cookies
- When the operator opens `/backoffice/login`
- Then the page heading reads "Backoffice Login"
- And the form has the fields "Username" and "Password" and the button "Login"

### Rule: A successful operator login opens the event publication log

#### login-opens-the-event-log
- Given the backoffice login page and the demo operator credentials "admin" / "admin" of a development run
- When the operator logs in with username "admin" and password "admin"
- Then the event log `/backoffice/events` opens with the heading "Event Publication Log"
- And its summary shows the counters "Total Events", "Completed" and "Incomplete", the total not negative

### Rule: Wrong credentials keep the operator on the login page with one message

#### wrong-password-is-refused
- Given the backoffice login page
- When the operator logs in with username "admin" and password "wrong"
- Then the login page `/backoffice/login?error=true` shows "Invalid username or password."

#### unknown-username-is-refused
- Given the backoffice login page
- When the operator logs in with username "operator" and password "admin"
- Then the login page shows "Invalid username or password."

## Out of scope

- The entries of the log and what the counters count — `the-event-log-lists-every-delivery`.
- Opening the back office without a login, and the operator logout — `the-back-office-stays-closed-to-others`.
- Which credentials a production run accepts: the operator account is configuration of the running shop, not
  behaviour of a page.

## Notes

- Replay story: `OPS-01` (run-the-shop).
- Shared scenario: `scenario.backoffice.login-shows-event-log`, `scenario.backoffice.invalid-login`.
- Inventory: backoffice B1, B2, B3.
