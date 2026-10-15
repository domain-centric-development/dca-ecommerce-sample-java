---
id: registration-leads-only-to-shop-pages
epic: account
status: approved
context: account
feature: registration
title: A registration leads only to pages of the shop
depends_on: [login-leads-only-to-shop-pages]
---

# A registration leads only to pages of the shop

## Story

As a visitor who registers from a link I followed I want the shop to lead me only to its own pages afterwards, so
that a link someone else built cannot send me, freshly logged in, to a site that looks like the shop.

## Acceptance criteria

### Rule: A return page outside the shop is not followed; the registration then leads to the home page

#### an-outside-return-page-leads-home-after-registering (happy path)
- Given a visitor with no cookies who opened `/register?returnUrl=https://elsewhere.example/`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then the home page `/` opens and shows "Account created successfully! Welcome!"

#### a-scheme-relative-return-page-leads-home-after-registering
- Given a visitor with no cookies who opened `/register?returnUrl=//elsewhere.example/`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then the home page `/` opens and shows "Account created successfully! Welcome!"

### Rule: The "Login here" link carries a return page on only when it is a page of the shop

#### the-login-link-drops-an-outside-return-page
- Given a visitor with no cookies
- When they open `/register?returnUrl=https://elsewhere.example/`
- Then the "Login here" link leads to `/login`

## Out of scope

- The login's return page: story `login-leads-only-to-shop-pages`.
- A return page of the shop's own, such as `/products`: it is followed as before, and the "Login here" link carries it
  on as before.

## Changed expectations

- A registration opened with the return page of another site sends the new customer to that site now; after, it
  leads to the shop's home page with "Account created successfully! Welcome!".
- The "Login here" link of a registration page opened with the return page of another site carries that return page
  on now; after, it leads to the login without one.

## Assumptions

- answered: a return page is followed only when it is a page of the shop (a relative path); otherwise the
  registration leads to the home page (planning/questions-2026-10-15.md Q4.2)
- answered: the "Login here" and "Register here" links keep a return page of the shop's own and drop any other
  (planning/questions-2026-10-15.md Q4.2)

## Notes

- From TODO #90 (open redirect), checked against both shops 2026-10-15: still true in both — the registration
  follows any return page that is not blank.
- Depends on `login-leads-only-to-shop-pages`: both decide which return page is a page of the shop, and one answer
  to that serves both.
- Spec (`account/registration`): adds `an-outside-return-page-leads-home-after-registering`,
  `a-scheme-relative-return-page-leads-home-after-registering` and `the-login-link-drops-an-outside-return-page`;
  holds `registration-returns-to-the-given-page`, `login-link-keeps-the-return-page` and
  `the-page-returned-to-says-the-account-was-created`.
