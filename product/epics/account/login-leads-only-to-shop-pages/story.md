---
id: login-leads-only-to-shop-pages
epic: account
status: approved
context: account
feature: signin
title: A login leads only to pages of the shop
depends_on: [only-the-browsers-own-guest-cart-is-taken-over]
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

### Rule: The "Register here" link carries a return page on only when it is a page of the shop

#### the-register-link-drops-an-outside-return-page
- Given a visitor with no cookies
- When they open `/login?returnUrl=https://elsewhere.example/`
- Then the "Register here" link leads to `/register`

## Out of scope

- The registration's return page: story `registration-leads-only-to-shop-pages`.
- The return page after the merge page: story `only-the-browsers-own-guest-cart-is-taken-over`.
- A return page of the shop's own, such as `/products`: it is followed as before, and the "Register here" link carries
  it on as before.

## Changed expectations

- A login opened with the return page of another site sends the customer to that site now; after, it opens the cart
  page with "Welcome back!", as a login without a return page does.
- The "Register here" link of a login page opened with the return page of another site carries that return page on
  now; after, it leads to the registration without one.

## Assumptions

- answered: a return page is followed only when it is a page of the shop (a relative path); otherwise a login, and a
  login through the merge page, opens the cart (planning/questions-2026-10-15.md Q4.2)
- answered: the "Login here" and "Register here" links keep a return page of the shop's own and drop any other
  (planning/questions-2026-10-15.md Q4.2)

## Notes

- From TODO #90 (open redirect), checked against both shops 2026-10-15: still true in both — the login hands any
  return page on, and the step after it follows it.
- Depends on `only-the-browsers-own-guest-cart-is-taken-over`: both change the way from the login to the next page,
  and that story changes the take-over and the merge first.
- Spec (`account/signin`): adds `an-outside-return-page-opens-the-cart-after-login`,
  `a-scheme-relative-return-page-opens-the-cart-after-login` and `the-register-link-drops-an-outside-return-page`;
  holds `login-returns-to-the-given-page`, `register-link-keeps-the-return-page` and `login-opens-the-cart`.
