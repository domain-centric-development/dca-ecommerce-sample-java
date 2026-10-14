---
id: the-back-office-header-is-the-operators
epic: run-the-shop
status: draft
context: backoffice
feature: access
title: The back office does not offer the shopper's login
depends_on: []
---

# The back office does not offer the shopper's login

## Story

As an operator logged in to the back office I want its pages to offer what concerns me, not the shopper's login and
registration, so that I am not told I am logged out while I am at work.

## Acceptance criteria

### Rule: A back-office page offers no shopper login, registration or cart

#### the-back-office-offers-no-shopper-login (happy path)
- Given the operator is logged in to the back office in a browser that is not logged in to the shop
- When they open `/backoffice/events`
- Then the page's header shows neither "Login" nor "Register"
- And it shows no "Cart" entry

## Changed expectations

- A back-office page shows the shop's header now: "Login", "Register" and the "Cart" entry for a browser not logged
  in to the shop, or the shopper's "Welcome, …" and "Logout" for one that is. After, a back-office page shows none of
  them.

## Assumptions

- open: what the back-office header shows instead — only the shop's name and the operator's "Logout"?
- open: the "Cart" entry goes as well (as written), or stays?
- open: a browser that is logged in to the shop as a shopper as well — should a back-office page hide that shopper's
  "Welcome, …" too (then a scenario of its own)?

## Notes

- From TODO #94 (e), checked 2026-10-15: still true in both shops — the back-office pages use the shop's layout.
- Spec (`backoffice/access`): adds `the-back-office-offers-no-shopper-login`; holds `login-opens-the-event-log`.
