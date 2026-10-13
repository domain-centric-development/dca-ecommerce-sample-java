---
id: the-profile-refuses-an-unusable-change
epic: account
status: adopted
context: account
feature: profile
title: The profile refuses an unusable change
depends_on: [keep-the-profile-up-to-date]
---

# The profile refuses an unusable change

## Story

As a customer I want the profile to refuse a change it cannot accept, keep what I typed and save nothing of
it, so that my details are never half changed.

## Acceptance criteria

<!-- happy-path: taken-address-is-refused-in-the-profile -->

### Rule: An address another account uses cannot be taken over

#### taken-address-is-refused-in-the-profile
- Given the accounts "anna.schmidt@example.com" and "ben.weber@example.com", and Anna logged in on `/account/profile`
- When she saves Email "ben.weber@example.com"
- Then the profile shows "This email address is already registered"
- And the field Email still holds "ben.weber@example.com"

### Rule: Address and date of birth must be usable

#### malformed-address-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Email "anna.meyer@example"
- Then the profile shows "Please enter a valid email address."

#### missing-date-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save an empty Date of Birth
- Then the profile shows "Please enter a date like 1990-05-17"

#### future-date-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Date of Birth 2999-01-01
- Then the profile shows "Please enter a date of birth that is not in the future."

#### unreadable-date-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Date of Birth "17.05.1990"
- Then the profile shows "Please enter a date like 1990-05-17"
- And the field Date of Birth still holds "17.05.1990"

### Rule: A refused change saves nothing, not even the valid field

#### refused-change-saves-nothing
- Given the customer "anna.schmidt@example.com", born 1990-05-17, saved Email "anna.meyer@example.com" together with Date of Birth 2999-01-01 and was refused
- When they open `/account/profile` again
- Then the fields hold "anna.schmidt@example.com" and 1990-05-17

## Out of scope

- Successful changes — `keep-the-profile-up-to-date`.

## Notes

- Replay story: `ACC-08` (account).
- The browser requires both fields and offers a date input; the refusals above are what the shop answers when
  such a value reaches it anyway.
- Inventory: account A24, A25.
