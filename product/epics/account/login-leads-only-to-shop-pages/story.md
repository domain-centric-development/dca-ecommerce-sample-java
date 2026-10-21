---
id: login-leads-only-to-shop-pages
epic: account
status: delivered
delivered: 2026-10-21T19:01:56Z
delivered_by: gate
created: 2026-10-14T23:44:13Z
context: account
feature: signin
title: The login page's register link leads only to pages of the shop
depends_on: [only-the-browsers-own-guest-cart-is-taken-over]
---

# The login page's register link leads only to pages of the shop

## Story

As a visitor who opens the login from a link I followed I want its "Register here" link to lead me only to pages of
the shop, so that a link someone else built cannot send me, freshly registered, to a site that looks like the shop.

## Acceptance criteria

### Rule: The "Register here" link carries a return page on only when it is a page of the shop

#### the-register-link-drops-an-outside-return-page (happy path)
- Given a visitor with no cookies
- When they open `/login?returnUrl=https://elsewhere.example/`
- Then the "Register here" link leads to `/register`

## Out of scope

- The registration's return page: story `registration-leads-only-to-shop-pages`.
- The login itself with a return page outside the shop: story `a-login-follows-only-a-return-page-of-the-shop`
  (adopted) — it opens the cart with "Welcome back!".
- The return page after the merge page: story `only-the-browsers-own-guest-cart-is-taken-over`.
- A return page of the shop's own, such as `/products`: it is followed as before, and the "Register here" link carries
  it on as before.

## Changed expectations

- The "Register here" link of a login page opened with the return page of another site carries that return page on
  now; after, it leads to the registration without one.

## Assumptions

- answered: a return page is followed only when it is a page of the shop (a relative path); otherwise a login, and a
  login through the merge page, opens the cart (planning/questions-2026-10-15.md Q4.2)
- answered: the "Login here" and "Register here" links keep a return page of the shop's own and drop any other
  (planning/questions-2026-10-15.md Q4.2)
- answered: the two login scenarios already hold — the step after the login follows a return page only when it is a
  page of the shop — so they move into the adopted story `a-login-follows-only-a-return-page-of-the-shop`, and this
  story builds only the register link (the user, 2026-10-21) (the .NET shop's decision login-leads-only-to-shop-pages-01, for both shops)

## Notes

- From TODO #90 (open redirect), checked against both shops 2026-10-15: still true in both — the login hands any
  return page on, and the step after it follows it.
- Depends on `only-the-browsers-own-guest-cart-is-taken-over`: both change the way from the login to the next page,
  and that story changes the take-over and the merge first.
- Spec (`account/signin`): adds `the-register-link-drops-an-outside-return-page`;
  holds `login-returns-to-the-given-page`, `register-link-keeps-the-return-page` and `login-opens-the-cart`.
