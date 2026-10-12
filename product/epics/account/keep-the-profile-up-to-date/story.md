---
id: keep-the-profile-up-to-date
epic: account
status: adopted
context: account
feature: profile
title: Keep the profile up to date
depends_on: [my-account-overview]
---

# Keep the profile up to date

## Story

As a customer I want to see my name and change my email address and date of birth, so that the shop always
reaches and knows me correctly, while my name stays as I registered it.

## Acceptance criteria

<!-- happy-path: saving-the-profile -->

### Rule: The profile shows the fixed name and the details that can change

#### profile-shows-name-and-details
- Given the customer "anna.schmidt@example.com", registered as Anna Schmidt born 1990-05-17, is logged in
- When they follow "My Profile" in the account navigation
- Then `/account/profile` shows the heading "My Profile", a card "Basic Information" and "My Profile" marked as the current page
- And the name "Anna Schmidt" is shown as text with "Your name was set when you created your account and cannot be changed." and there is no input for a name
- And the field "Email" holds "anna.schmidt@example.com" with the hint "This address is also your login"
- And the field "Date of Birth" holds 1990-05-17 and the button reads "Save Changes"

### Rule: Saving stores email and date of birth and keeps the customer logged in

#### saving-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they change Email to "anna.meyer@example.com" and Date of Birth to 1990-05-18 and press "Save Changes"
- Then `/account/profile` opens again and shows "Your profile has been updated."
- And the fields hold "anna.meyer@example.com" and 1990-05-18
- And the header shows "Welcome, anna.meyer@example.com"

#### own-address-in-other-capitals-is-accepted
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Email "Anna.Schmidt@Example.com" with the same date of birth
- Then the profile shows "Your profile has been updated." and the field Email holds "anna.schmidt@example.com"

### Rule: The new address is the login from now on

#### new-address-logs-in
- Given the customer changed the address from "anna.schmidt@example.com" to "anna.meyer@example.com" and a browser with no cookies
- When they log in with "anna.meyer@example.com" and "Secret123"
- Then the cart page opens and the header shows "Welcome, anna.meyer@example.com"

#### old-address-no-longer-logs-in
- Given the customer changed the address from "anna.schmidt@example.com" to "anna.meyer@example.com" and a browser with no cookies
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then the login page shows "Invalid email or password"

### Rule: The profile needs a login

#### profile-sends-a-visitor-to-the-login
- Given a visitor with no cookies
- When they open `/account/profile`
- Then they are sent to `/login?returnUrl=%2Faccount%2Fprofile`

## Out of scope

- Refused profile changes — `the-profile-refuses-an-unusable-change`.
- Changing the name: the name is fixed at registration; the shop offers no way to change it.

## Notes

- Replay story: `ACC-07` (account).
- Inventory: account A19 (profile), A22, A23, A24 (own address accepted).
