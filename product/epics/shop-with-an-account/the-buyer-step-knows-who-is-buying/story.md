---
id: the-buyer-step-knows-who-is-buying
epic: shop-with-an-account
status: adopted
context: checkout
feature: buyerdetails
title: The buyer step knows who is buying
depends_on: [place-the-order-from-the-review-and-see, logging-in-brings-the-guest-cart-along, the-cart-stays-with-the-person]
---

# The buyer step knows who is buying

## Story

As a shopper at the checkout's buyer step I want to be offered a login or a registration while I am a guest,
and to find my account's email already filled in once I am logged in, so that an account saves me typing
without being required to buy.

## Acceptance criteria

<!-- happy-path: logged-in-customer-sees-who-is-buying -->

### Rule: A guest on the buyer step is offered to log in or register

#### guest-is-offered-login-and-registration
- Given a guest whose cart holds "Domain-Driven Design"
- When they press "Proceed to Checkout" on the cart page
- Then the buyer step `/checkout/buyer` says "Already have an account? Log in to speed up checkout."
- And it offers a "Login" button to `/login?returnUrl=/checkout/buyer` and a "Register" button to `/register?returnUrl=/checkout/buyer`
- And the "Email Address" field is empty

### Rule: A logged-in customer sees whom they are logged in as, with the account's email filled in

#### logged-in-customer-sees-who-is-buying
- Given a guest whose cart holds "Domain-Driven Design", who opened the buyer step and then registered on `/register` with "anna.schmidt@example.com"
- When they open the buyer step again
- Then it says "Logged in as anna.schmidt@example.com" instead of offering a login or a registration
- And the "Email Address" field holds "anna.schmidt@example.com"

#### customer-orders-with-the-account-email
- Given the customer "anna.schmidt@example.com" is logged in, added "Domain-Driven Design" and pressed "Proceed to Checkout"
- And they kept the filled-in email on the buyer step, entered "Anna", "Schmidt" and "+49 30 1234567", and completed the delivery and payment steps
- When they press "Place Order" on the review step
- Then the confirmation page reads "Your order has been placed successfully"
- And it shows "anna.schmidt@example.com" under "Contact Information"

### Rule: A guest who registers in the middle of the checkout finishes it logged in

#### guest-registers-mid-checkout-and-orders
- Given a guest whose cart holds "Domain-Driven Design", who pressed "Proceed to Checkout", followed "Register" on the buyer step and registered with "anna.schmidt@example.com"
- And back on the buyer step they kept the filled-in email, entered "Anna", "Schmidt" and "+49 30 1234567", and completed the delivery and payment steps
- When they press "Place Order" on the review step
- Then the confirmation page reads "Your order has been placed successfully"
- And it shows "anna.schmidt@example.com" under "Contact Information"
- And the header shows "Welcome, anna.schmidt@example.com"

## Out of scope

- The buyer form and its validation — `enter-the-buyer-details`; the review and confirmation pages — `place-the-order-from-the-review-and-see`.
- Where registering or logging in from the checkout leads, and what happens to the open checkout — `the-cart-stays-with-the-person`, `logging-in-brings-the-guest-cart-along`.

## Notes

- Replay story: `ACX-04` (shop-with-an-account).
- An email already entered on the buyer step in this checkout wins over the account's email when the step is
  opened again (`go-back-to-an-earlier-step-and-change-it` shows entered details again). Before this story nothing competes with it, so a test
  would be green before the build: a guarantee for the plan stage, not a criterion.
- Shared scenarios: `scenario.checkout.buyer-step-reflects-login`, `scenario.checkout.authenticated-completes`.
- Inventory: checkout 22, 23, 26 (a logged-in customer completes the same checkout).
