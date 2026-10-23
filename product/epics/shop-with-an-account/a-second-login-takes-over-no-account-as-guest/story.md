---
id: a-second-login-takes-over-no-account-as-guest
epic: shop-with-an-account
status: draft
created: 2026-10-23T21:25:20Z
context: account
feature: signin
title: A second login takes over no account as a guest
closes: [only-the-browsers-own-guest-cart-is-taken-over#1]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# A second login takes over no account as a guest

## Story

As a customer who logs in to a second account without logging out I want the first account's cart to stay with it, so that logging in never moves one account's cart into another.

## Acceptance criteria

### Rule: Only a guest's cart follows a login

#### a-login-over-another-account-moves-no-cart (happy path)
- Given the account "anna.schmidt@example.com", whose cart holds "Clean Architecture" once, logged in
- When the same browser logs in to the account "ben.weber@example.com" without logging out
- Then Ben's cart holds nothing it did not hold before
- And Anna's cart still holds "Clean Architecture" with quantity 1

## Assumptions

- open: logging in while logged in to another account — the first account's cart stays where it is, and the login is otherwise an ordinary one?

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- Same finding in both shops: the login records the logged-in account as the guest whose cart may follow.
