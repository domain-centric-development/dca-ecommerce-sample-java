---
id: register-an-account
epic: account
status: adopted
context: account
feature: registration
title: Register an account
depends_on: [CAT-01]
---

# Register an account

## Story

As a visitor I want to create an account with my name, date of birth, email and a password, and be logged in
right away, so that the shop knows me the next time I come back.

## Acceptance criteria

<!-- happy-path: registration-logs-the-visitor-in -->

### Rule: A visitor can reach the registration from every shop page

#### header-offers-register
- Given a visitor with no cookies
- When they open the catalogue page `/products`
- Then the header shows a "Register" link to `/register`

### Rule: The registration form asks for the owner, the address and the password

#### register-page-shows-the-form
- Given a visitor with no cookies
- When they open `/register`
- Then the page heading reads "Create Account"
- And the form has the fields "First Name", "Last Name", "Date of Birth", "Email", "Password" and "Confirm Password" and the button "Create Account"
- And it shows the hints "Your name cannot be changed later" and "Password must be at least 8 characters"
- And the name fields accept at most 100 characters and the password fields at most 72
- And a link "Login here" leads to `/login`

### Rule: A successful registration logs the visitor in

#### registration-logs-the-visitor-in
- Given a visitor with no cookies on `/register`
- When they submit First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17, Email "anna.schmidt@example.com", Password "Secret123" and Confirm Password "Secret123"
- Then they are sent to the home page `/`
- And the header shows "Welcome, anna.schmidt@example.com" instead of the "Register" link

#### registration-returns-to-the-given-page
- Given a visitor with no cookies who opened `/register?returnUrl=/products`
- When they submit a valid registration for "anna.schmidt@example.com"
- Then they are sent to the catalogue page `/products`
- And the header shows "Welcome, anna.schmidt@example.com"

#### login-link-keeps-the-return-page
- Given a visitor with no cookies
- When they open `/register?returnUrl=/products`
- Then the "Login here" link leads to `/login?returnUrl=/products`

## Out of scope

- The "Login" link, the login itself and the "Logout" button — `log-in-and-log-out`.
- The "My Account" link — `my-account-overview`.
- Refusals of the form (passwords that differ, a short password, an unreadable or future date, a malformed
  address, a missing or too long name) — `registration-refuses-an-unusable-form`.
- An address that is already registered, and a second registration from the same browser — `one-account-per-address-and-per-browser`.
- The password rules beyond the length hint (uppercase, lowercase, digit, 72 bytes) — `the-password-policy`.
- Keeping the guest cart when registering, and registering from the checkout — `shop-with-an-account`.
- Registration through the API — `shop-api`.

## Notes

- Replay story: `ACC-01` (account).
- Inventory: account A1 (header, registration part), A2, A3.
