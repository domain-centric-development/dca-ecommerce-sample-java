---
id: my-account-overview
epic: account
status: adopted
context: account
feature: profile
title: My account overview
depends_on: [log-in-and-log-out]
---

# My account overview

## Story

As a customer I want one place that shows my account and leads to everything I can change about it, so that I
can check what the shop knows about me and when my account was last used.

## Acceptance criteria

<!-- happy-path: overview-shows-the-account -->

### Rule: A logged-in customer reaches the overview from the header

#### header-offers-my-account
- Given the customer "anna.schmidt@example.com" is logged in
- When they open the catalogue page `/products`
- Then the header shows a "My Account" link to `/account`

### Rule: The overview shows the address and the last login

#### overview-shows-the-account
- Given the customer "anna.schmidt@example.com" has just logged in
- When they follow "My Account" in the header
- Then `/account` shows the heading "Welcome, anna.schmidt@example.com" and a card "Account Overview"
- And the card shows "Email" with "anna.schmidt@example.com"
- And "Last Login" shows the date and time of this login in UTC in the form dd.MM.yyyy HH:mm, for example "25.09.2026 14:05"

#### overview-after-registration-shows-never
- Given a visitor who has just registered "anna.schmidt@example.com" and has never logged in
- When they open `/account`
- Then "Last Login" shows "Never"

### Rule: Every account page has the same left navigation

#### navigation-lists-the-account-pages
- Given the customer "anna.schmidt@example.com" is logged in
- When they open `/account`
- Then the account navigation lists, in this order, "Overview" linking to `/account`, "My Profile" linking to `/account/profile`, "Change Password" linking to `/account/change-password` and "My Orders"
- And "Overview" is marked as the current page

#### my-orders-is-not-a-link
- Given the customer "anna.schmidt@example.com" is logged in
- When they open `/account`
- Then "My Orders" is shown as disabled and is not a link

### Rule: The account area needs a login and returns to the page asked for

#### overview-sends-a-visitor-to-the-login
- Given a visitor with no cookies
- When they open `/account`
- Then they are sent to `/login?returnUrl=%2Faccount`

#### login-returns-to-the-overview
- Given the account "anna.schmidt@example.com" and a visitor who was sent from `/account` to the login page
- When they log in with "anna.schmidt@example.com" and "Secret123"
- Then `/account` opens with "Welcome, anna.schmidt@example.com"

## Out of scope

- The profile page — `keep-the-profile-up-to-date`; the change-password page — `change-the-password`.
- Orders: "My Orders" is a disabled placeholder; the shop has no order list.

## Notes

- Replay story: `ACC-06` (account).
- Inventory: account A1 ("My Account" link), A17, A18, A19 (overview).
