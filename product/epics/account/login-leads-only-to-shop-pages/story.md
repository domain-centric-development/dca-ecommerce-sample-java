---
id: login-leads-only-to-shop-pages
epic: account
status: draft
context: account
feature: signin
title: A login leads only to pages of the shop
depends_on: [taking-over-the-guest-cart-is-a-step-of-its-own]
---

# A login leads only to pages of the shop

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

- The registration's return page: story `registration-leads-only-to-shop-pages`.
- The return page after the merge page: story `only-the-browsers-own-guest-cart-is-taken-over`.
- A return page of the shop's own, such as `/products`: it is followed as before (`login-returns-to-the-given-page`).

## Changed expectations

- A login opened with the return page of another site sends the customer to that site now; after, it opens the cart
  page with "Welcome back!", as a login without a return page does.

## Assumptions

- open: the page an outside return page is replaced with — the cart page, as for a login without a return page (as
  written), or another one?
- open: the "Register here" link of a login page opened with an outside return page — leave the return page out of
  the link, or keep it?

## Notes

- From TODO #90 (open redirect), checked against both shops 2026-10-15: still true in both — the login hands any
  return page on, and the step after it follows it.
- Depends on the take-over story because both change the way from the login to the next page.
- Spec (`account/signin`): adds `an-outside-return-page-opens-the-cart-after-login` and
  `a-scheme-relative-return-page-opens-the-cart-after-login`; holds `login-returns-to-the-given-page` and
  `login-opens-the-cart`.
