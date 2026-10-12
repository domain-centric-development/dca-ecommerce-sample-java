---
id: registration-refuses-an-unusable-form
epic: account
status: adopted
context: account
feature: registration
title: Registration refuses an unusable form
depends_on: [register-an-account]
---

# Registration refuses an unusable form

## Story

As a visitor I want the registration to tell me exactly which entry it cannot accept, and keep what I typed,
so that I can correct it without starting over.

## Acceptance criteria

<!-- happy-path: differing-passwords-are-refused -->

### Rule: A refused registration shows one message and keeps everything but the passwords

#### differing-passwords-are-refused
- Given a visitor with no cookies on `/register`
- When they submit First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17, Email "anna.schmidt@example.com", Password "Secret123" and Confirm Password "Secret124"
- Then the registration page shows "Passwords do not match"
- And First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17 and Email "anna.schmidt@example.com" are still filled in, both password fields are empty
- And the visitor is not logged in

### Rule: The password needs at least 8 characters

#### short-password-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Password and Confirm Password "Sec123"
- Then the registration page shows "Password must be at least 8 characters"

### Rule: The date of birth must be a real date, and not in the future

#### unreadable-date-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Date of Birth "17.05.1990"
- Then the registration page shows "Please enter a date like 1990-05-17"

#### future-date-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Date of Birth 2999-01-01
- Then the registration page shows "Date of birth cannot be in the future"

### Rule: The address must be an email address

#### malformed-email-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Email "anna.schmidt@example"
- Then the registration page shows "Invalid email format: anna.schmidt@example"

### Rule: Both names are required and at most 100 characters long

#### blank-first-name-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with First Name "   "
- Then the registration page shows "First name is required"

#### overlong-last-name-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with a Last Name of 101 characters
- Then the registration page shows "Last name is too long"

## Out of scope

- An address that is already registered, and a second registration from the same browser — `one-account-per-address-and-per-browser`.
- The password rules for uppercase, lowercase, digit and 72 bytes — `the-password-policy`.
- Registration refusals through the API — `shop-api`.

## Notes

- Replay story: `ACC-03` (account).
- The browser limits the fields (required, 100 and 72 characters, date input); the refusals above are what the
  shop answers when such a value reaches it anyway.
- Inventory: account A5, A6, A7, A10.
