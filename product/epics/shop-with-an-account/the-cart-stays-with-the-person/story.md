---
id: the-cart-stays-with-the-person
epic: shop-with-an-account
status: adopted
context: account
feature: signin
title: The cart stays with the person through registration, logout and a lapsed login
depends_on: [register-an-account, log-in-and-log-out, logging-in-brings-the-guest-cart-along, start-the-checkout-from-the-cart]
---

# The cart stays with the person through registration, logout and a lapsed login

## Story

As a shopper I want the cart I filled as a guest to become my account's cart when I register, the next person
on my device to start with an empty cart after I log out, and my cart to stay when my login lapses, so that
the cart always belongs to me and only to me.

## Acceptance criteria

<!-- happy-path: registering-keeps-the-guest-cart -->

### Rule: Registering keeps the guest cart: the new account takes it over

#### registering-keeps-the-guest-cart
- Given a guest who added "Domain-Driven Design" to the cart
- When they register as Anna Schmidt, born 1990-05-17, with "anna.schmidt@example.com" and password "Secret123"
- Then the cart page shows one line "Domain-Driven Design" with quantity 1
- And the header shows "Welcome, anna.schmidt@example.com"

#### registering-from-the-checkout-keeps-the-checkout
- Given a guest whose cart holds "Domain-Driven Design", who pressed "Proceed to Checkout" and then opened `/register?returnUrl=/checkout/buyer`
- When they register as Anna Schmidt, born 1990-05-17, with "anna.schmidt@example.com" and password "Secret123"
- Then the buyer step `/checkout/buyer` opens
- And its order summary shows "Domain-Driven Design x1"

#### registered-customer-adds-to-the-cart
- Given a visitor who just registered with "anna.schmidt@example.com"
- When they press "Add to Cart" on the product page of "Clean Architecture"
- Then the cart holds "Clean Architecture"

#### taken-over-cart-comes-back-at-the-next-login
- Given a guest who added "Domain-Driven Design", registered with "anna.schmidt@example.com" and then logged out
- When they log in again with "anna.schmidt@example.com" and "Secret123"
- Then the cart shows one line "Domain-Driven Design" with quantity 1

### Rule: Logging out gives the browser a fresh, empty guest cart

#### logout-leaves-an-empty-cart
- Given the customer "anna.schmidt@example.com" is logged in and their cart holds "Domain-Driven Design"
- And they pressed "Logout" in the header
- When they open the cart page
- Then the cart page shows "Your cart is empty"

### Rule: If a login session lapses, the visitor keeps the cart they had

#### lapsed-login-keeps-the-cart
- Given the account "anna.schmidt@example.com", registered earlier, and a browser in which a guest added `"Ubiquitous Language" Mug` and then logged in to that account
- When the login session expires after its 7 days, before the browser's visitor identity does
- Then the header shows "Login" and "Register" again
- And the cart page still shows `"Ubiquitous Language" Mug` with quantity 1

## Out of scope

- The registration form and where it leads — `register-an-account`; logout and its message — `log-in-and-log-out`.
- The guest cart moving into the account at login — `logging-in-brings-the-guest-cart-along`; the choice between two filled carts — `choose-how-two-carts-come-together`.
- The "Register" button on the buyer step and what the buyer step shows a logged-in customer — `the-buyer-step-knows-who-is-buying`.
- A shop restart, which forgets accounts and carts — `accounts-start-fresh-with-the-shop`.

## Notes

- Replay story: `ACX-02` (shop-with-an-account).
- Registering shows no merge page: the account takes over the guest's browser identity, so there is only one cart.
- Shared scenarios: `scenario.identity.registered-user-adds-to-cart`, `scenario.checkout.login-returns-to-checkout`.
- Inventory: cart C-07, C-48, C-49, C-50, C-51; checkout 24; account A4, A16, A35 (the cart is kept), A36.
