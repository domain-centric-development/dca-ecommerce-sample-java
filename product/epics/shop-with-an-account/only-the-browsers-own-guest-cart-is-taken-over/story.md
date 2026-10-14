---
id: only-the-browsers-own-guest-cart-is-taken-over
epic: shop-with-an-account
status: draft
context: cart
feature: cartrecovery
title: Logging in takes over only the guest cart of this browser
depends_on: [taking-over-the-guest-cart-is-a-step-of-its-own]
---

# Logging in takes over only the guest cart of this browser

## Story

As a guest who fills a cart I want no one else's login to take my cart away, so that what I collected stays mine
until I log in myself; and as a customer who logs in I want the shop to lead me only to its own pages afterwards.

## Acceptance criteria

### Rule: The take-over and the merge move only the cart of the guest this browser was before the login

#### another-guests-cart-is-not-taken-over (happy path)
- Given the account "anna.schmidt@example.com", whose cart holds nothing, logged in in a browser that had an empty
  guest cart
- And in another browser a guest added "Clean Architecture" to the cart
- When Anna's browser sends the cart take-over for the other browser's guest cart
- Then the cart page `/cart` opens and Anna's cart holds nothing
- And the other guest's cart still holds "Clean Architecture" with quantity 1

#### another-guests-cart-is-not-merged
- Given the account "anna.schmidt@example.com", whose cart holds "Clean Architecture" once, logged in in a browser
  that had an empty guest cart
- And in another browser a guest added `"Ubiquitous Language" Mug` to the cart
- When Anna's browser sends the merge choice "Merge Both Carts" for the other browser's guest cart
- Then the cart page `/cart` opens and Anna's cart holds only "Clean Architecture" with quantity 1
- And the other guest's cart still holds `"Ubiquitous Language" Mug` with quantity 1

### Rule: The merge page leads only to pages of the shop

#### an-outside-return-page-after-the-merge-opens-the-cart
- Given the two carts, and the guest opened `/login?returnUrl=https://elsewhere.example/`, logged in and is on the
  merge page
- When they press "Continue" with "Merge Both Carts" chosen
- Then the cart page `/cart` opens and shows "Carts merged successfully!"

## Out of scope

- The return page of a login that needs no merge page: story `login-leads-only-to-shop-pages`.

## Changed expectations

- A logged-in customer's browser can name any guest's cart in the take-over or the merge now, and that cart's lines
  move into the account while the other guest's cart is gone; after, only the guest cart this browser had before the
  login is taken over, and a request for another one changes no cart.
- The merge page sends the customer on to a return page of another site now; after, it opens the cart page.

## Assumptions

- open: what the customer sees when their browser asks for another guest's cart — the cart page as it is, without a
  message (as written), or the cart page with a message, and which?
- open: the page an outside return page is replaced with after the merge — the cart page (as written)?

## Notes

- From TODO #90 (unchecked guest identity, open redirect), checked against both shops 2026-10-15: still true in both
  — the guest cart to take over travels with the browser's request and is not compared with the guest the shop knew
  before the login; the merge and the take-over follow any return page that is not blank.
- Depends on `taking-over-the-guest-cart-is-a-step-of-its-own` (approved in the Java shop, delivered in the .NET
  shop): it brings the take-over step these scenarios send.
- Spec (`cart/cartrecovery`): adds `another-guests-cart-is-not-taken-over`, `another-guests-cart-is-not-merged` and
  `an-outside-return-page-after-the-merge-opens-the-cart`; holds `merge-both-adds-up-the-carts` and
  `continue-takes-the-guest-cart-over`.
