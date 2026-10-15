---
id: the-back-office-header-is-the-operators
epic: run-the-shop
status: approved
context: backoffice
feature: access
title: The back office's header is the operator's
depends_on: []
---

# The back office's header is the operator's

## Story

As an operator logged in to the back office I want its header to offer what concerns me, not the shopper's login,
registration or cart, so that I am not told I am logged out while I am at work.

## Acceptance criteria

### Rule: A back-office page's header shows the shop's name, "Back office" and the operator's logout — nothing of a shopper

#### the-back-office-header-offers-the-operators-logout (happy path)
- Given the operator is logged in to the back office in a browser that is not logged in to the shop
- When they open `/backoffice/events`
- Then the page's header shows the shop's name, "Back office" and a "Logout" button
- And it shows neither "Login" nor "Register" nor a "Cart" entry

#### the-back-office-header-shows-no-shopper
- Given a browser logged in to the shop as "anna.schmidt@example.com" whose operator then logged in to the back office
- When they open `/backoffice/events`
- Then the page's header shows neither "Welcome, anna.schmidt@example.com" nor "My Account" nor a "Cart" entry

## Changed expectations

- A back-office page shows the shop's header now: "Login", "Register" and the "Cart" entry for a browser not logged
  in to the shop, or the shopper's "Welcome, …", "My Account" and "Logout" for one that is. After, it shows the shop's
  name, "Back office" and the operator's "Logout", and nothing of a shopper.

## Assumptions

- answered: the back-office header shows the shop's name, "Back office" and the operator's Logout; no cart, no
  account, no logged-in shopper (planning/questions-2026-10-15.md Q4.5)

## Notes

- From TODO #94 (e), checked 2026-10-15: still true in both shops — the back-office pages use the shop's layout.
- The header's "Logout" is the operator's, the one the event log's own "Logout" button already is
  (`logout-confirms-on-the-login-page`).
- Spec (`backoffice/access`): adds `the-back-office-header-offers-the-operators-logout` and
  `the-back-office-header-shows-no-shopper`; holds `login-opens-the-event-log` and `logout-confirms-on-the-login-page`.
