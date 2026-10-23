---
id: the-checkout-after-a-login-is-the-guests
epic: shop-with-an-account
status: draft
created: 2026-10-23T21:25:20Z
context: checkout
feature: session
title: The checkout after a login is the guest's
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# The checkout after a login is the guest's

## Story

As a guest who logs in from the checkout I want to return to the checkout I started, not to an older open checkout of my account.

## Acceptance criteria

### Rule: A login from the checkout returns to the guest's checkout

#### a-login-returns-to-the-guests-checkout-not-an-older-one (happy path)
- Given an account with an open checkout from a cart it emptied since
- And a guest who started a checkout of "Clean Architecture"
- When the guest logs in to that account from the checkout
- Then the checkout shows "Clean Architecture", not the account's older checkout

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
