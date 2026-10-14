---
id: registration-leads-only-to-shop-pages
epic: account
status: draft
context: account
feature: registration
title: A registration leads only to pages of the shop
depends_on: []
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

## Out of scope

- The login's return page: story `login-leads-only-to-shop-pages`.
- A return page of the shop's own, such as `/products`: it is followed as before (`registration-returns-to-the-given-page`).

## Changed expectations

- A registration opened with the return page of another site sends the new customer to that site now; after, it
  leads to the shop's home page with "Account created successfully! Welcome!".

## Assumptions

- open: the page an outside return page is replaced with — the home page, as for a registration without a return
  page (as written), or another one?
- open: the "Login here" link of a registration page opened with an outside return page — leave the return page out
  of the link, or keep it (the login ignores it then)?

## Notes

- From TODO #90 (open redirect), checked against both shops 2026-10-15: still true in both — the registration
  follows any return page that is not blank.
- Spec (`account/registration`): adds `an-outside-return-page-leads-home-after-registering` and
  `a-scheme-relative-return-page-leads-home-after-registering`; holds `registration-returns-to-the-given-page` and
  `the-page-returned-to-says-the-account-was-created`.
