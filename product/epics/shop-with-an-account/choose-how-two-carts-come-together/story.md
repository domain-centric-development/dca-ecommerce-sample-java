---
id: choose-how-two-carts-come-together
epic: shop-with-an-account
status: adopted
context: cart
feature: cartrecovery
title: Choose how two carts come together
depends_on: [logging-in-brings-the-guest-cart-along]
---

# Choose how two carts come together

## Story

As a customer who filled a cart as a guest while my account's cart also holds products, I want to see both
carts when I log in and decide whether to merge them or keep one, so that I end up with the cart I mean to
buy.

## Acceptance criteria

<!-- happy-path: merge-both-adds-up-the-carts -->

In every scenario "the two carts" are: the account "anna.schmidt@example.com" with password "Secret123",
registered earlier, whose cart holds "Clean Architecture" with quantity 1, added while logged in in another
browser; and a browser with no cookies in which a guest added "Clean Architecture" twice and
`"Ubiquitous Language" Mug` once.

### Rule: If both carts hold items, logging in asks what to do with them

#### merge-page-when-both-carts-hold-items
- Given the two carts
- When the guest logs in with "anna.schmidt@example.com" and "Secret123"
- Then the merge page opens with the heading "Your Cart Has Items"
- And the sentence "You have items in both your current browsing session and your account. How would you like to proceed?"

#### merge-page-summarises-both-carts
- Given the two carts
- When the guest logs in with "anna.schmidt@example.com" and "Secret123"
- Then the box "Current Session Cart" shows "Items: 2", "Total Quantity: 3" and "Total: 96.97 EUR", and lists "Clean Architecture" with "Qty: 2" at "39.99 EUR" and `"Ubiquitous Language" Mug` with "Qty: 1" at "16.99 EUR", each with its image
- And the box "Account Cart" shows "Items: 1", "Total Quantity: 1" and "Total: 39.99 EUR", and lists "Clean Architecture" with "Qty: 1" at "39.99 EUR"

#### merge-page-offers-three-options
- Given the two carts
- When the guest logs in with "anna.schmidt@example.com" and "Secret123"
- Then under "Choose an Option" the page offers "Merge Both Carts" — "Combine all items from both carts. If the same product exists in both, quantities will be added together." — already selected
- And "Use Account Cart Only" — "Keep your account cart and discard the items from your current session."
- And "Use Current Session Cart Only" — "Replace your account cart with the items from your current session."
- And a "Continue" button

### Rule: "Merge Both Carts" adds the guest lines to the account's cart, adding up quantities of the same product

#### merge-both-adds-up-the-carts
- Given the two carts, and the guest logged in and is on the merge page
- When they press "Continue" with "Merge Both Carts" chosen
- Then the cart page opens and shows "Carts merged successfully!"
- And the cart shows "Clean Architecture" with quantity 3 and `"Ubiquitous Language" Mug` with quantity 1

### Rule: "Use Account Cart Only" keeps the account's cart and drops the guest lines

#### use-account-cart-only
- Given the two carts, and the guest logged in and is on the merge page
- When they press "Continue" with "Use Account Cart Only" chosen
- Then the cart page opens and shows "Using your account cart."
- And the cart shows only "Clean Architecture" with quantity 1

### Rule: "Use Current Session Cart Only" replaces the account's cart with the guest lines

#### use-current-session-cart-only
- Given the two carts, and the guest logged in and is on the merge page
- When they press "Continue" with "Use Current Session Cart Only" chosen
- Then the cart page opens and shows "Using your recent cart."
- And the cart shows "Clean Architecture" with quantity 2 and `"Ubiquitous Language" Mug` with quantity 1

### Rule: A choice the page does not offer is refused

#### unknown-merge-option-is-refused
- Given the two carts, and the guest logged in and is on the merge page
- When the merge form is sent with an option other than the three offered
- Then the merge page opens again and shows "Invalid merge option selected"

## Out of scope

- Only one of the two carts holding items: no question is asked — `logging-in-brings-the-guest-cart-along`.
- Logging in from the checkout's buyer step — `logging-in-brings-the-guest-cart-along`.

## Notes

- Replay story: `ACX-03` (shop-with-an-account).
- The merge page's unit prices and totals are the prices the lines were added at; no surface changes a price
  today, so they equal the catalogue prices.
- Shared scenario: `scenario.cart.merge-on-login`.
- Inventory: cart C-38, C-39, C-40, C-41, C-42, C-43, C-45.
