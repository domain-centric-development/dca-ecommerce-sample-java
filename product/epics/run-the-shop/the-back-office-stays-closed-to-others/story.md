---
id: the-back-office-stays-closed-to-others
epic: run-the-shop
status: adopted
context: backoffice
feature: access
title: The back office stays closed to others
depends_on: [operator-login-to-the-back-office, register-an-account]
---

# The back office stays closed to others

## Story

As an operator I want the back office to open only after an operator login, and to close again when I log
out, so that neither a visitor nor a shop customer can read what the shop did.

## Acceptance criteria

<!-- happy-path: event-log-sends-a-visitor-to-the-login -->

### Rule: Every back-office page needs an operator login

#### event-log-sends-a-visitor-to-the-login
- Given a browser with no cookies that is not logged in to the back office
- When it opens `/backoffice/events` directly
- Then the shop sends it to the backoffice login page `/backoffice/login`

#### a-customer-login-does-not-open-the-back-office
- Given a visitor who has just registered the account "anna.schmidt@example.com" and is logged in to the shop
- When they open `/backoffice/events`
- Then the shop sends them to the backoffice login page `/backoffice/login`

#### login-after-a-redirect-opens-the-event-log
- Given a browser that opened `/backoffice/settings` and was sent to the backoffice login page
- When the operator logs in with username "admin" and password "admin"
- Then the event log `/backoffice/events` opens

### Rule: Logging out closes the back office and says so

#### logout-confirms-on-the-login-page
- Given the operator is logged in on `/backoffice/events`
- When they press "Logout"
- Then the backoffice login page `/backoffice/login?logout=true` opens and shows "You have been logged out."

#### event-log-is-closed-after-logout
- Given the operator has logged out of the back office
- When they open `/backoffice/events` again
- Then the shop sends them to the backoffice login page `/backoffice/login`

## Out of scope

- The operator login itself and wrong credentials — `operator-login-to-the-back-office`.

## Notes

- Replay story: `OPS-04` (run-the-shop).
- Shared scenario: `scenario.backoffice.unauthenticated-redirect`, `scenario.backoffice.logout`.
- Inventory: backoffice B2 (always the log), B4, B11.
