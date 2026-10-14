---
id: taking-over-the-guest-cart-is-a-step-of-its-own
epic: shop-with-an-account
status: approved
context: cart
feature: cartrecovery
title: Logging in takes the guest cart over in a step the browser sends
depends_on: []
---

# Logging in takes the guest cart over in a step the browser sends

## Story

As a guest who logs in with items in the cart I want my cart taken into my account by a step my browser sends, not
by merely opening a page, so that a page opened by itself — a preview, a reload, a link followed twice — never
moves my cart.

## Acceptance criteria

### Rule: Opening the page that follows the login moves no cart; the take-over is a form the browser sends

#### the-page-after-the-login-moves-no-cart
- Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier, whose cart holds nothing
- And a browser with no cookies that runs no script, in which a guest added `"Ubiquitous Language" Mug` to the cart
- When the guest logs in on `/login` with "anna.schmidt@example.com" and "Secret123"
- Then a page opens that says "Bringing your cart along." and offers a "Continue" button
- And the account's cart still holds nothing

#### continue-takes-the-guest-cart-over (happy path)
- Given the account "anna.schmidt@example.com", whose cart holds nothing, and a guest in a browser that runs no script
  who added `"Ubiquitous Language" Mug`, logged in and sees "Bringing your cart along."
- When they press "Continue"
- Then the cart page `/cart` opens and shows "Welcome back!"
- And the cart shows one line `"Ubiquitous Language" Mug` with quantity 1

## Out of scope

- The merge page for two carts that both hold items: it stays as it is, and its "Continue" already sends the choice.
- How the browser that runs scripts gets on: it sends the form by itself, and the login leads on to the cart as it
  does today — the existing scenarios of logging in with a guest cart keep holding.

## Changed expectations

- Logging in with a guest cart takes it into the account as soon as the page after the login is opened now; after,
  it is taken over when that page's form is sent — by the browser itself, or with "Continue" where the browser runs
  no script.

## Notes

- Decided by the user 2026-10-14 (WP-108 part A, DCA-HEX-014): no state changes on a page that is only opened. The
  visible flow stays as close to today's as it can: with scripts the customer sees the cart page as now, without them
  one more page with one button.
