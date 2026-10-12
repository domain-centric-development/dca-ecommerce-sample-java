---
id: enter-the-buyer-details
epic: guest-checkout
status: adopted
context: checkout
feature: buyerdetails
title: Enter the buyer details
depends_on: [start-the-checkout-from-the-cart]
---

# Enter the buyer details

## Story

As a guest in the checkout I want to tell the shop who I am and how to reach me, so that the order is
placed in my name.

## Acceptance criteria

<!-- happy-path: valid-details-open-the-delivery-step -->

### Rule: The buyer step asks for email, first name, last name and phone number

#### buyer-step-shows-the-form
- Given a guest who started the checkout with "Domain-Driven Design" in the cart
- When the guest opens the buyer step
- Then it shows the heading "Buyer Information"
- And the fields "Email Address", "First Name", "Last Name" and "Phone Number", all empty and all required
- And a "Continue to Delivery" button

#### valid-details-open-the-delivery-step
- Given a guest on the buyer step
- When the guest submits Email Address "jane@example.com", First Name "Jane", Last Name "Doe" and Phone
  Number "+49 30 1234567"
- Then the delivery step opens at `/checkout/delivery`

### Rule: Invalid buyer details keep the guest on the buyer step with a message

#### invalid-email-and-missing-phone-keep-the-buyer-step
- Given a guest on the buyer step
- When the guest submits Email Address "not-an-email", First Name "Jane", Last Name "Doe" and no Phone Number
- Then the buyer step stays open and shows a validation error
- And the delivery step is not reached

#### email-without-at-sign-is-refused
- Given a guest on the buyer step
- When the buyer form is sent past the browser's own checks with Email Address "jane.example.com", First Name
  "Jane", Last Name "Doe" and Phone Number "+49 30 1234567"
- Then the buyer step opens again with the error "Email must be a valid email address"

#### blank-first-name-is-refused
- Given a guest on the buyer step
- When the guest submits Email Address "jane@example.com", First Name "   " (spaces only), Last Name "Doe" and
  Phone Number "+49 30 1234567"
- Then the buyer step opens again with the error "First name cannot be null or blank"

#### blank-phone-is-refused
- Given a guest on the buyer step
- When the guest submits Email Address "jane@example.com", First Name "Jane", Last Name "Doe" and Phone
  Number "   " (spaces only)
- Then the buyer step opens again with the error "Phone cannot be null or blank"

## Out of scope

- The delivery step behind "Continue to Delivery" — `choose-the-delivery-address`.
- Coming back to the buyer step with the details filled in — `go-back-to-an-earlier-step-and-change-it`.
- The "Already have an account?" offer with Login and Register, and the email filled in for a logged-in
  customer — `shop-with-an-account`.

## Notes

- Replay story: `CHK-02` (guest-checkout).
- Shared scenario: `scenario.checkout.buyer-validation`.
- Inventory: checkout 20, 21.
