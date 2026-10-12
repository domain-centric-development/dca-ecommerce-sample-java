---
id: log-in-and-log-out
epic: account
status: adopted
context: account
feature: signin
title: Log in and log out
depends_on: [register-an-account, the-cart-page]
---

# Log in and log out

## Story

As a customer with an account I want to log in with my email and password and log out again, so that I can
use my account on any browser and leave nothing of it behind on a shared one.

## Acceptance criteria

<!-- happy-path: login-opens-the-cart -->

### Rule: A visitor can reach the login from every shop page

#### header-offers-login
- Given a visitor with no cookies
- When they open the catalogue page `/products`
- Then the header shows a "Login" link to `/login` next to the "Register" link

#### login-page-shows-the-form
- Given a visitor with no cookies
- When they open `/login`
- Then the page heading reads "Login"
- And the form has the fields "Email" and "Password" and the button "Login"
- And a link "Register here" leads to `/register`

### Rule: A successful login starts the session and opens the cart, or the page the customer came for

#### login-opens-the-cart
- Given the account "anna.schmidt@example.com" with password "Secret123", registered earlier, and a browser with no cookies and an empty cart
- When the visitor logs in on `/login` with "anna.schmidt@example.com" and "Secret123"
- Then the cart page `/cart` opens and shows "Welcome back!"
- And the header shows "Welcome, anna.schmidt@example.com" and a "Logout" button instead of "Login" and "Register"

#### login-returns-to-the-given-page
- Given the account "anna.schmidt@example.com" and a browser with no cookies that opened `/login?returnUrl=/products`
- When the visitor logs in with "anna.schmidt@example.com" and "Secret123"
- Then the catalogue page `/products` opens

#### register-link-keeps-the-return-page
- Given a visitor with no cookies
- When they open `/login?returnUrl=/products`
- Then the "Register here" link leads to `/register?returnUrl=/products`

### Rule: Logging out ends the session and says so

#### logout-confirms-on-the-login-page
- Given the customer "anna.schmidt@example.com" is logged in
- When they press "Logout" in the header
- Then the login page `/login?logout=true` opens and shows "You have been successfully logged out."
- And the header shows "Login" and "Register" again

## Out of scope

- A wrong or unknown email or password — `a-failed-login-gives-nothing-away`.
- The merge page shown when both the guest cart and the account's cart hold items, the empty cart after logout
  and the account's cart coming back at the next login — `shop-with-an-account`.
- Login and logout through the API — `shop-api`.
- The lifetime of the session and the visitor identity, and which cookies are set — cross-cutting, outside this epic.

## Notes

- Replay story: `ACC-02` (account).
- Inventory: account A1 (login and logout part), A11, A12, A15.
