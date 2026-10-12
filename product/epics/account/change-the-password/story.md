---
id: change-the-password
epic: account
status: adopted
context: account
feature: password
title: Change the password
depends_on: [my-account-overview]
---

# Change the password

## Story

As a customer I want to replace my password with a new one, so that I can keep my account safe when I think
the old one is known to someone else.

## Acceptance criteria

<!-- happy-path: changing-the-password -->

### Rule: The change-password page asks for the current password and the new one twice

#### change-password-page-shows-the-form
- Given the customer "anna.schmidt@example.com" is logged in
- When they follow "Change Password" in the account navigation
- Then `/account/change-password` shows the heading "Change Password" and "Change Password" marked as the current page
- And the form has the empty fields "Current Password", "New Password" and "Confirm New Password" and the button "Change Password"
- And "New Password" carries the hint "Password must be at least 8 characters and contain an uppercase letter, a lowercase letter and a digit"
- And the new-password fields accept at most 72 characters

### Rule: A successful change keeps the customer logged in

#### changing-the-password
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123", New Password "Better456" and Confirm New Password "Better456"
- Then `/account/change-password` opens again and shows "Your password has been changed."
- And all three password fields are empty
- And the header still shows "Welcome, anna.schmidt@example.com"

#### keeping-the-same-password-is-allowed
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit "Secret123" as current, new and confirmed password
- Then the page shows "Your password has been changed."

### Rule: From then on only the new password logs in

#### new-password-logs-in
- Given the customer "anna.schmidt@example.com" changed the password from "Secret123" to "Better456", and a browser with no cookies
- When they log in with "anna.schmidt@example.com" and "Better456"
- Then the cart page opens and the header shows "Welcome, anna.schmidt@example.com"

#### old-password-no-longer-logs-in
- Given the customer "anna.schmidt@example.com" changed the password from "Secret123" to "Better456", and a browser with no cookies
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then the login page shows "Invalid email or password"

### Rule: The change-password page needs a login

#### change-password-sends-a-visitor-to-the-login
- Given a visitor with no cookies
- When they open `/account/change-password`
- Then they are sent to `/login?returnUrl=%2Faccount%2Fchange-password`

## Out of scope

- Refused changes (confirmation, current password) — `a-refused-password-change-changes`; the password rules — `the-password-policy`.

## Notes

- Replay story: `ACC-09` (account).
- Inventory: account A19 (change password), A26, A27.
