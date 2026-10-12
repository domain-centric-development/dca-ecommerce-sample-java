---
id: logging-in-brings-the-guest-cart-along
epic: shop-with-an-account
status: adopted
context: cart
feature: cartrecovery
title: Logging in brings the guest cart along
depends_on: [log-in-and-log-out, the-cart-stays-with-the-guest, without-an-open-checkout-the-steps-lead]
---

# Logging in brings the guest cart along

## Story

As a guest who filled a cart and then logs in to my account I want to find the products I collected in my
account's cart, and my account's cart when I collected nothing, so that logging in never costs me what I
already chose.

## Acceptance criteria

<!-- happy-path: guest-cart-follows-into-the-account -->

### Rule: If only the guest cart holds items, they move into the account's cart without a question

#### guest-cart-follows-into-the-account
- Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier, whose cart holds nothing
- And a browser with no cookies in which a guest added `"Ubiquitous Language" Mug` to the cart
- When the guest logs in on `/login` with "anna.schmidt@example.com" and "Secret123"
- Then the cart page `/cart` opens and shows "Welcome back!"
- And the cart shows one line `"Ubiquitous Language" Mug` with quantity 1

### Rule: If the guest cart is empty, the account's cart is shown as it is

#### account-cart-shown-after-login
- Given the account "anna.schmidt@example.com", registered earlier, which added "Clean Architecture" to its cart while logged in in another browser
- And a browser with no cookies and an empty cart
- When the visitor logs in there with "anna.schmidt@example.com" and "Secret123"
- Then the cart page shows one line "Clean Architecture" with quantity 1

### Rule: Logging in from the checkout keeps the guest's cart, not the guest's open checkout

#### login-from-the-checkout-keeps-the-cart-not-the-checkout
- Given the account "anna.schmidt@example.com", registered earlier, whose cart holds nothing
- And a browser with no cookies in which a guest added "Domain-Driven Design" and pressed "Proceed to Checkout"
- And the guest opened `/login?returnUrl=/checkout/buyer`
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then the cart page opens with the error "No active checkout session found"
- And the cart shows one line "Domain-Driven Design" with quantity 1

### Rule: A logged-in customer shops and checks out like a guest

#### logged-in-customer-starts-the-checkout
- Given the account "anna.schmidt@example.com", registered earlier, and a browser with no cookies
- And the visitor logged in with "anna.schmidt@example.com" and "Secret123" and added "Domain-Driven Design" to the cart
- When they press "Proceed to Checkout" on the cart page
- Then the buyer step opens at `/checkout/buyer`

## Out of scope

- The choice offered when both the guest cart and the account's cart hold items — `choose-how-two-carts-come-together`.
- Registration keeping the guest cart, the empty cart after logout and a lapsed login — `the-cart-stays-with-the-person`.
- The login form, "Welcome back!" for an empty cart and the return to a given page — `log-in-and-log-out`.
- The "Login" button on the buyer step — `the-buyer-step-knows-who-is-buying`.

## Assumptions

- answered: What happens to a guest's open checkout when they log in to an existing account from the buyer
  step? — It stays behind with the guest: the cart moves into the account, the visitor lands on the cart with "No
  active checkout session found" and starts the checkout again (user, 2026-09-25).

## Notes

- Replay story: `ACX-01` (shop-with-an-account).
- Shared scenario: `scenario.identity.existing-account-checkout`.
- Inventory: cart C-37, C-44, C-46, C-47 (the guest cart is empty); checkout 25; account A12 (the cart part).
