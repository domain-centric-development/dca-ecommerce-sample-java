---
id: the-take-over-leads-only-to-pages-of-the-shop
epic: shop-with-an-account
status: adopted
created: 2026-10-23T21:25:20Z
context: cart
feature: cartrecovery
title: The take-over leads only to pages of the shop
depends_on: []
---

# The take-over leads only to pages of the shop

## Story

As a guest who logs in from a link I followed I want the take-over of my cart to lead me only to pages of the shop, so that a link someone else built cannot send me to a site that looks like the shop.

## Acceptance criteria

### Rule: The take-over leads only to pages of the shop

#### an-outside-return-page-after-the-take-over-opens-the-cart (happy path)
- Given an account whose cart is empty, and a guest cart holding "Clean Architecture" once
- And the guest opened `/login?returnUrl=https://elsewhere.example/` and logged in to that account
- When the take-over of the guest cart is sent
- Then the cart page `/cart` opens and holds "Clean Architecture" with quantity 1

## Assumptions

- answered: the guest identity the take-over moves is bound to the session already — only-the-browsers-own-guest-cart-is-taken-over acts only when the form's guest matches the guest the identity was before the login; this story is the return page alone (taken over from the .NET sample's release, the user, 2026-10-24).
- answered: the take-over POST already sends an outside return page to `/cart` (`CartMergePageController.isShopPage`); no test proves it for the take-over, only for the merge. Released as `adopted` (the user, 2026-10-24): the adoption writes the missing test with its break.

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- WP-151 decision 4: N79, a security finding ranked minor, goes first; every other clean-up item depends on it.
- The merge path holds this already in both shops (`an-outside-return-page-after-the-merge-opens-the-cart`); the take-over POST is the gap.
- Java checks the return page on the take-over already (`CartMergePageController.isShopPage`): the user decided (2026-10-23) to release it there as `adopted`; .NET builds it.
