---
id: a-login-follows-only-a-return-page-of-the-shop
epic: account
status: delivered
delivered: 2026-10-21T16:08:39Z
delivered_by: gate
created: 2026-10-21T15:00:00Z
context: account
feature: signin
title: A login follows only a return page of the shop
depends_on: [only-the-browsers-own-guest-cart-is-taken-over]
---

# A login follows only a return page of the shop

## Story

As a customer who logs in from a link I followed I want the shop to lead me only to its own pages afterwards, so
that a link someone else built cannot send me, freshly logged in, to a site that looks like the shop.

## Acceptance criteria

### Rule: A return page outside the shop is not followed; the login then opens the cart

#### an-outside-return-page-opens-the-cart-after-login (happy path)
- Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier
- And a browser with no cookies and an empty cart that opened `/login?returnUrl=https://elsewhere.example/`
- When the visitor logs in with "anna.schmidt@example.com" and "Secret123"
- Then the cart page `/cart` opens and shows "Welcome back!"

#### a-scheme-relative-return-page-opens-the-cart-after-login
- Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier
- And a browser with no cookies and an empty cart that opened `/login?returnUrl=//elsewhere.example/`
- When the visitor logs in with "anna.schmidt@example.com" and "Secret123"
- Then the cart page `/cart` opens and shows "Welcome back!"

## Out of scope

- The "Register here" link of the login page: story `login-leads-only-to-shop-pages`.
- The return page after the merge page: story `only-the-browsers-own-guest-cart-is-taken-over`.
- The registration's return page: story `registration-leads-only-to-shop-pages`.

## Assumptions

- answered: a return page is followed only when it is a page of the shop (a relative path); otherwise a login, and a
  login through the merge page, opens the cart (planning/questions-2026-10-15.md Q4.2)
- answered: the two login scenarios already hold once `only-the-browsers-own-guest-cart-is-taken-over` is delivered —
  the step after the login follows a return page only when it is a page of the shop, also when no merge page is
  needed — so they are adopted in a story of their own, each test with a break (the redirect that follows any return
  page); `login-leads-only-to-shop-pages` builds only the register link (the user, 2026-10-21, in the .NET shop's
  decision login-leads-only-to-shop-pages-01, option a, for both shops)

## Notes

- Taken out of `login-leads-only-to-shop-pages`, 2026-10-21. The behaviour came with
  `only-the-browsers-own-guest-cart-is-taken-over` (its check of the return page serves the login without a merge
  page too), but no scenario and no test carried that path.
- Spec (`account/signin`): adds `an-outside-return-page-opens-the-cart-after-login` and
  `a-scheme-relative-return-page-opens-the-cart-after-login`.
