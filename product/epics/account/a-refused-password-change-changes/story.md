---
id: a-refused-password-change-changes
epic: account
status: adopted
context: account
feature: password
title: A refused password change changes nothing
depends_on: [change-the-password]
---

# A refused password change changes nothing

## Story

As a customer I want a password change to go through only when I know the current password and typed the new
one the same way twice, so that a typo or someone at my open browser cannot lock me out.

## Acceptance criteria

<!-- happy-path: differing-confirmation-is-refused -->

### Rule: The confirmation must match the new password — checked first

#### differing-confirmation-is-refused
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Secret123", New Password "Better456" and Confirm New Password "Better457"
- Then the page shows "New password and confirmation do not match"
- And all three password fields are empty

#### differing-confirmation-wins-over-a-wrong-current-password
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Wrong999", New Password "Better456" and Confirm New Password "Better457"
- Then the page shows "New password and confirmation do not match"

### Rule: The current password must be correct — checked before the new password's strength

#### wrong-current-password-is-refused
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Wrong999", New Password "Better456" and Confirm New Password "Better456"
- Then the page shows "Current password is not correct"

#### wrong-current-password-wins-over-a-weak-new-one
- Given the customer "anna.schmidt@example.com" with password "Secret123" is logged in on `/account/change-password`
- When they submit Current Password "Wrong999", New Password "weakpass" and Confirm New Password "weakpass"
- Then the page shows "Current password is not correct"

### Rule: After a refusal the old password still holds

#### refused-change-keeps-the-old-password
- Given the customer "anna.schmidt@example.com" tried to change "Secret123" with Current Password "Wrong999" and was refused, and a browser with no cookies
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then the cart page opens and the header shows "Welcome, anna.schmidt@example.com"

## Out of scope

- The strength messages for the new password — `the-password-policy`.

## Notes

- Replay story: `ACC-10` (account).
- Inventory: account A28.
