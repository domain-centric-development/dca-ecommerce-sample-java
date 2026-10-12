---
id: the-password-policy
epic: account
status: adopted
context: account
feature: password
title: The password policy
depends_on: [register-an-account, change-the-password]
---

# The password policy

## Story

As a customer I want the shop to accept only passwords that are hard to guess, and to tell me which rule a
password breaks, so that my account is protected and I know how to fix my choice.

## Acceptance criteria

<!-- happy-path: password-without-uppercase-is-refused -->

### Rule: At least 8 characters — checked before anything else

#### short-new-password-is-refused
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123" and "Abc1234" as new and confirmed password
- Then the page shows "Password must be at least 8 characters long"

#### length-is-checked-before-the-characters
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123" and "abcdefg" as new and confirmed password
- Then the page shows "Password must be at least 8 characters long"

### Rule: At most 72 bytes in UTF-8

#### password-over-72-bytes-is-refused
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123" and, as new and confirmed password, "Aa1" followed by 35 times "ä" (38 characters, 73 bytes)
- Then the page shows "Password must not be longer than 72 bytes (UTF-8 encoded)"

#### password-of-72-bytes-is-accepted
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123" and, as new and confirmed password, "Aa1" followed by 34 times "ä" and one "b" (38 characters, 72 bytes)
- Then the page shows "Your password has been changed."

### Rule: At least one uppercase letter, one lowercase letter and one digit

#### password-without-uppercase-is-refused
- Given a visitor with no cookies on `/register`
- When they submit a registration for "anna.schmidt@example.com" with Password and Confirm Password "secret123"
- Then the registration page shows "Password must contain at least one uppercase letter"
- And the visitor is not logged in

#### new-password-without-lowercase-is-refused
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123" and "SECRET123" as new and confirmed password
- Then the page shows "Password must contain at least one lowercase letter"

#### new-password-without-digit-is-refused
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123" and "SecretPass" as new and confirmed password
- Then the page shows "Password must contain at least one digit"

## Out of scope

- The registration page's own length check ("Password must be at least 8 characters") — `registration-refuses-an-unusable-form`.
- The API's password refusals — `shop-api`.

## Notes

- Replay story: `ACC-11` (account).
- The browser's minimum length of 8 on the new-password field keeps a short password from being sent; the
  length refusal is what the shop answers when one reaches it anyway.
- Inventory: account A29.
